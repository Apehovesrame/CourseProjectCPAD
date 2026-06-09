package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.dao.BusDaoImpl;
import ru.pin123.courseprojectcpad.dao.DriverDaoImpl;
import ru.pin123.courseprojectcpad.dao.RouteDaoImpl;
import ru.pin123.courseprojectcpad.dao.TripDaoImpl;
import ru.pin123.courseprojectcpad.model.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер модального диалогового окна для создания и редактирования рейса.
 * Отвечает за выбор маршрута, автобуса и водителей, ввод даты и времени отправления,
 * автоматический расчет времени прибытия и сохранение данных рейса в базу данных.
 */
public class TripEditController implements Initializable {

    /** Логгер для фиксации событий создания и редактирования рейсов. */
    private static final Logger logger = LoggerFactory.getLogger(TripEditController.class);

    /** Выпадающий список для выбора маршрута рейса. */
    @FXML private ComboBox<Route> comboRoutes;
    /** Выпадающий список для выбора автобуса. */
    @FXML private ComboBox<Bus> comboBuses;
    /** Список доступных водителей с поддержкой множественного выбора. */
    @FXML private ListView<Driver> listDrivers;
    /** Поле выбора даты отправления. */
    @FXML private DatePicker dpDeparture;
    /** Поле ввода времени отправления (формат HH:mm). */
    @FXML private TextField tfTime;
    /** Метка для отображения рассчитанного времени прибытия. */
    @FXML private Label lblArrival;

    /** DAO для работы с маршрутами. */
    private final RouteDaoImpl routeDao = new RouteDaoImpl();
    /** DAO для работы с автобусами. */
    private final BusDaoImpl busDao = new BusDaoImpl();
    /** DAO для работы с водителями. */
    private final DriverDaoImpl driverDao = new DriverDaoImpl();
    /** DAO для работы с рейсами. */
    private final TripDaoImpl tripDao = new TripDaoImpl();

    /** Ссылка на модальное окно диалога. */
    private Stage dialogStage;
    /** Объект рейса, данные которого редактируются (или пустой для создания нового). */
    private Trip trip;
    /** Рассчитанное время прибытия на основе даты, времени отправления и длительности маршрута. */
    private LocalDateTime calculatedArrival = null;
    /** Флаг, указывающий на то, что пользователь подтвердил сохранение данных (нажал ОК). */
    private boolean isOkClicked = false;

    /**
     * Устанавливает ссылку на модальное окно диалога.
     * @param dialogStage объект Stage для управления окном.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Возвращает статус подтверждения сохранения данных пользователем.
     * @return true, если пользователь нажал кнопку сохранения, иначе false.
     */
    public boolean isOkClicked() {
        return isOkClicked;
    }

    /**
     * Инициализирует форму данными существующего рейса при редактировании.
     * Заполняет комбобоксы и поля ввода значениями из объекта {@link Trip}.
     *
     * @param trip объект рейса для редактирования.
     */
    public void setTrip(Trip trip) {
        this.trip = trip;
        if (trip.getTripId() != null) {
            // Ищем и выбираем нужный маршрут и автобус в выпадающих списках
            comboRoutes.getItems().stream().filter(r -> r.getRouteId() == trip.getRoute().getRouteId()).findFirst().ifPresent(comboRoutes::setValue);
            comboBuses.getItems().stream().filter(b -> b.getBusId() == trip.getBus().getBusId()).findFirst().ifPresent(comboBuses::setValue);

            // Устанавливаем дату и время
            dpDeparture.setValue(trip.getDepartureDatetime().toLocalDate());
            tfTime.setText(String.format("%02d:%02d", trip.getDepartureDatetime().getHour(), trip.getDepartureDatetime().getMinute()));

            updateArrivalTime();
            logger.debug("Форма заполнена данными рейса ID [{}] для редактирования.", trip.getTripId());
        }
    }

    /**
     * Инициализирует контроллер после загрузки FXML-файла.
     * Загружает списки маршрутов, автобусов и водителей из БД,
     * настраивает слушатели событий для автоматического пересчета времени прибытия.
     *
     * @param location  URL-адрес для разрешения относительных путей, или null.
     * @param resources Ресурсы для локализации, или null.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            comboRoutes.setItems(FXCollections.observableArrayList(routeDao.findAll()));
            comboBuses.setItems(FXCollections.observableArrayList(busDao.findAll()));
            listDrivers.setItems(FXCollections.observableArrayList(driverDao.findAll()));
            listDrivers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            dpDeparture.setValue(LocalDate.now());

            logger.info("Инициализация формы редактирования рейса. Загружено маршрутов: {}, автобусов: {}, водителей: {}.",
                    comboRoutes.getItems().size(), comboBuses.getItems().size(), listDrivers.getItems().size());

            comboRoutes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateArrivalTime());
            dpDeparture.valueProperty().addListener((obs, oldVal, newVal) -> updateArrivalTime());
            tfTime.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) updateArrivalTime();
            });

        } catch (Exception e) {
            logger.error("Критическая ошибка при инициализации формы редактирования рейса и загрузке справочных данных.", e);
            showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
        }
    }

    /**
     * Пересчитывает и отображает время прибытия на основе выбранного маршрута, даты и времени отправления.
     * Вызывается автоматически при изменении любого из этих параметров.
     */
    private void updateArrivalTime() {
        Route route = comboRoutes.getValue();
        LocalDate date = dpDeparture.getValue();
        String timeStr = tfTime.getText() != null ? tfTime.getText().trim() : "";

        if (route != null && date != null && !timeStr.isEmpty()) {
            try {
                String[] timeParts = timeStr.split(":");
                int hours = Integer.parseInt(timeParts[0]);
                int mins = Integer.parseInt(timeParts[1]);

                LocalDateTime departure = date.atTime(hours, mins);
                calculatedArrival = departure.plusMinutes(route.getDurationMinutes());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                lblArrival.setText(calculatedArrival.format(formatter));
                lblArrival.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

                logger.debug("Время прибытия пересчитано: отправление {} + {} мин = прибытие {}.",
                        departure, route.getDurationMinutes(), calculatedArrival);

            } catch (Exception e) {
                logger.warn("Не удалось рассчитать время прибытия. Некорректный формат времени: '{}'.", timeStr);
                lblArrival.setText("Ошибка формата (Нужно HH:mm)");
                lblArrival.setStyle("-fx-text-fill: red;");
                calculatedArrival = null;
            }
        }
    }

    /**
     * Обрабатывает нажатие кнопки сохранения.
     * Валидирует введенные данные, устанавливает значения в объект модели {@link Trip}
     * и сохраняет его в БД (создает новый или обновляет существующий) вместе со списком водителей.
     *
     * @param event событие клика по кнопке сохранения.
     */
    @FXML
    public void onSaveTripClick(ActionEvent event) {
        try {
            User currentUser = Session.getCurrentUser();
            Route selectedRoute = comboRoutes.getValue();
            Bus selectedBus = comboBuses.getValue();
            List<Driver> selectedDrivers = new ArrayList<>(listDrivers.getSelectionModel().getSelectedItems());

            if (selectedRoute == null || selectedBus == null || calculatedArrival == null) {
                logger.warn("Попытка сохранения рейса прервана: не заполнены обязательные поля или некорректное время.");
                showAlert(Alert.AlertType.WARNING, "Внимание", "Заполните все поля и проверьте формат времени!");
                return;
            }

            if (selectedDrivers.isEmpty()) {
                logger.warn("Попытка сохранения рейса прервана: не выбран ни один водитель.");
                showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите хотя бы одного водителя для этого рейса!");
                return;
            }

            String[] timeParts = tfTime.getText().trim().split(":");
            LocalDateTime departureDatetime = dpDeparture.getValue().atTime(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));

            trip.setRoute(selectedRoute);
            trip.setBus(selectedBus);
            trip.setDepartureDatetime(departureDatetime);
            trip.setArrivalDatetime(calculatedArrival);
            if (trip.getTripId() == null) trip.setCreatedByUser(currentUser);

            if (trip.getTripId() == null) {
                tripDao.create(trip, selectedDrivers); // Создание нового
                logger.info("Создан новый рейс: Маршрут №{} (ID: {}), Автобус {} (Гос. номер: {}), Время отправления: {}, Водителей: {}.",
                        selectedRoute.getRouteNumber(), selectedRoute.getRouteId(), selectedBus.getModel(),
                        selectedBus.getLicensePlate(), departureDatetime, selectedDrivers.size());
            } else {
                tripDao.update(trip, selectedDrivers); // Обновление старого
                logger.info("Обновлены данные рейса ID [{}]: Маршрут №{}, Автобус {}, Время отправления: {}, Водителей: {}.",
                        trip.getTripId(), selectedRoute.getRouteNumber(), selectedBus.getModel(),
                        departureDatetime, selectedDrivers.size());
            }

            isOkClicked = true;
            dialogStage.close();
        } catch (Exception e) {
            logger.error("Критическая ошибка при сохранении рейса в базу данных.", e);
            showAlert(Alert.AlertType.ERROR, "Ошибка сохранения", e.getMessage());
        }
    }

    /**
     * Обрабатывает нажатие кнопки Отмена. Закрывает диалоговое окно без сохранения изменений.
     */
    @FXML
    private void handleCancel() {
        logger.debug("Редактирование рейса отменено пользователем.");
        dialogStage.close();
    }

    /**
     * Отображает модальное всплывающее окно с сообщением для пользователя.
     *
     * @param type    тип предупреждения (INFO, WARNING, ERROR и т.д.).
     * @param title   заголовок окна.
     * @param content текстовое содержание сообщения.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}