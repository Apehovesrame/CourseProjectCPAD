package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.dao.TripDaoImpl;
import ru.pin123.courseprojectcpad.model.Trip;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер для управления списком рейсов в JavaFX приложении.
 * Отвечает за отображение таблицы рейсов с форматированными датами,
 * а также за выполнение операций CRUD (создание, чтение, обновление, удаление)
 * через модальные диалоговые окна. Взаимодействует с базой данных через {@link TripDaoImpl}.
 */
public class TripsController implements Initializable {

    /** Логгер для фиксации событий управления рейсами. */
    private static final Logger logger = LoggerFactory.getLogger(TripsController.class);

    /** Таблица для отображения списка рейсов. */
    @FXML private TableView<Trip> tripTable;
    /** Колонка с информацией о маршруте. */
    @FXML private TableColumn<Trip, String> colRoute;
    /** Колонка с датой и временем отправления. */
    @FXML private TableColumn<Trip, String> colDeparture;
    /** Колонка с датой и временем прибытия. */
    @FXML private TableColumn<Trip, String> colArrival;
    /** Колонка с информацией об автобусе. */
    @FXML private TableColumn<Trip, String> colBus;

    /** DAO-объект для работы с базой данных рейсов. */
    private final TripDaoImpl tripDao = new TripDaoImpl();
    /** Наблюдаемый список рейсов для привязки к таблице. */
    private final ObservableList<Trip> tripList = FXCollections.observableArrayList();

    /**
     * Инициализирует контроллер после загрузки FXML-файла.
     * Настраивает привязку данных для колонок таблицы (включая форматирование дат),
     * и загружает данные из БД.
     *
     * @param location  URL-адрес для разрешения относительных путей, или null.
     * @param resources Ресурсы для локализации, или null.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Инициализация контроллера рейсов. Настройка колонок таблицы.");

        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colBus.setCellValueFactory(new PropertyValueFactory<>("bus"));

        // Красивое форматирование дат в таблице
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        colDeparture.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDepartureDatetime().format(formatter)));
        colArrival.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getArrivalDatetime().format(formatter)));

        tripTable.setItems(tripList);
        loadData();
    }

    /**
     * Загружает список всех рейсов из базы данных и обновляет таблицу.
     * В случае ошибки выводит сообщение в лог.
     */
    private void loadData() {
        try {
            tripList.clear();
            tripList.addAll(tripDao.findAll());
            logger.info("Успешно загружено {} записей рейсов из базы данных.", tripList.size());
        } catch (Exception e) {
            logger.error("Критическая ошибка при попытке загрузки списка рейсов из слоя DAO", e);
        }
    }

    /**
     * Обработчик нажатия кнопки "Создать рейс".
     * Открывает диалоговое окно для ввода данных нового рейса.
     */
    @FXML
    public void handleCreateTrip() {
        logger.debug("Открытие диалога для назначения нового рейса.");
        Trip newTrip = new Trip();
        showTripEditDialog(newTrip);
    }

    /**
     * Открывает модальное диалоговое окно для создания или редактирования данных рейса.
     * После успешного сохранения обновляет таблицу.
     *
     * @param trip объект рейса с данными для отображения в диалоге (пустой для создания нового).
     */
    private void showTripEditDialog(Trip trip) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/trip-edit-view.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(trip.getTripId() == null ? "Назначение нового рейса" : "Редактирование рейса");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            TripEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTrip(trip); // Передаем рейс!

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                logger.info("Данные рейса успешно сохранены через диалоговое окно. Обновление таблицы.");
                loadData(); // Обновляем таблицу, если нажали "Сохранить"
            } else {
                logger.debug("Диалоговое окно рейса закрыто без сохранения изменений.");
            }
        } catch (IOException e) {
            logger.error("Критическая ошибка ввода-вывода интерфейса при загрузке fxml-формы trip-edit-view.fxml", e);
        }
    }

    /**
     * Обработчик нажатия кнопки "Редактировать".
     * Открывает диалоговое окно для изменения данных выбранного рейса.
     * Если рейс не выбран, показывает предупреждение.
     */
    @FXML
    private void handleEditTrip() {
        Trip selectedTrip = tripTable.getSelectionModel().getSelectedItem();
        if (selectedTrip != null) {
            logger.debug("Открытие диалога для редактирования рейса ID [{}].", selectedTrip.getTripId());
            showTripEditDialog(selectedTrip);
        } else {
            logger.warn("Попытка редактирования: действие отменено, рейс не выбран в таблице.");
            showAlert("Выберите рейс в таблице для редактирования.");
        }
    }

    /**
     * Обработчик нажатия кнопки "Удалить".
     * Запрашивает подтверждение пользователя и удаляет выбранный рейс из базы данных.
     * Если рейс не выбран, показывает предупреждение.
     */
    @FXML
    private void handleDeleteTrip() {
        Trip selectedTrip = tripTable.getSelectionModel().getSelectedItem();
        if (selectedTrip != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение удаления");
            confirm.setHeaderText("Удаление рейса");
            confirm.setContentText("Вы уверены, что хотите удалить этот рейс? Проданные на него билеты сохранятся в истории.");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    tripDao.delete(selectedTrip.getTripId());
                    logger.info("Из базы данных успешно удален рейс ID [{}].", selectedTrip.getTripId());
                    loadData(); // Обновляем таблицу
                } catch (Exception e) {
                    logger.error("Ошибка при удалении рейса ID [{}] из базы данных.", selectedTrip.getTripId(), e);
                    showAlert("Ошибка при удалении: " + e.getMessage());
                }
            } else {
                logger.debug("Удаление рейса ID [{}] отменено пользователем.", selectedTrip.getTripId());
            }
        } else {
            logger.warn("Попытка удаления: действие отменено, рейс не выбран в таблице.");
            showAlert("Выберите рейс в таблице для удаления.");
        }
    }

    /**
     * Отображает модальное всплывающее окно с предупреждением для пользователя.
     *
     * @param content текстовое содержание предупреждения.
     */
    private void showAlert(String content) {
        new Alert(Alert.AlertType.WARNING, content).showAndWait();
    }
}