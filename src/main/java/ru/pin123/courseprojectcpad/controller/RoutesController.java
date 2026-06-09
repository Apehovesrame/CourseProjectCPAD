package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.dao.RouteDaoImpl;
import ru.pin123.courseprojectcpad.model.Route;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер для управления списком маршрутов в JavaFX приложении.
 * Отвечает за отображение таблицы маршрутов, а также за выполнение операций CRUD
 * (создание, чтение, обновление, удаление) через модальные диалоговые окна.
 * Взаимодействует с базой данных через {@link RouteDaoImpl}.
 */
public class RoutesController implements Initializable {

    /** Логгер для фиксации событий управления маршрутами. */
    private static final Logger logger = LoggerFactory.getLogger(RoutesController.class);

    /** Таблица для отображения списка маршрутов. */
    @FXML private TableView<Route> routeTable;
    /** Колонка с номером маршрута. */
    @FXML private TableColumn<Route, String> colNumber;
    /** Колонка с пунктом отправления. */
    @FXML private TableColumn<Route, String> colFrom;
    /** Колонка с пунктом назначения. */
    @FXML private TableColumn<Route, String> colTo;
    /** Колонка с длительностью маршрута (в формате "чч ч мм мин"). */
    @FXML private TableColumn<Route, String> colDuration;

    /** DAO-объект для работы с базой данных маршрутов. */
    private final RouteDaoImpl routeDao = new RouteDaoImpl();
    /** Наблюдаемый список маршрутов для привязки к таблице. */
    private final ObservableList<Route> routeList = FXCollections.observableArrayList();

    /**
     * Инициализирует контроллер после загрузки FXML-файла.
     * Настраивает привязку данных для колонок таблицы и загружает данные из БД.
     *
     * @param location  URL-адрес для разрешения относительных путей, или null.
     * @param resources Ресурсы для локализации, или null.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNumber.setCellValueFactory(new PropertyValueFactory<>("routeNumber"));
        colFrom.setCellValueFactory(new PropertyValueFactory<>("departurePoint"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("destinationPoint"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));

        routeTable.setItems(routeList);
        loadData();
    }

    /**
     * Загружает список всех маршрутов из базы данных и обновляет таблицу.
     * В случае ошибки выводит сообщение в лог.
     */
    private void loadData() {
        try {
            routeList.clear();
            routeList.addAll(routeDao.findAll());
            logger.info("Успешно загружено {} записей маршрутов из базы данных.", routeList.size());
        } catch (Exception e) {
            logger.error("Критическая ошибка при попытке загрузки списка маршрутов из слоя DAO", e);
        }
    }

    /**
     * Обработчик нажатия кнопки "Добавить".
     * Открывает диалоговое окно для ввода данных нового маршрута.
     * Если пользователь подтвердил ввод, сохраняет новый маршрут в БД и обновляет таблицу.
     */
    @FXML
    private void handleAdd() {
        Route tempRoute = new Route();
        boolean okClicked = showRouteEditDialog(tempRoute);
        if (okClicked) {
            routeDao.save(tempRoute);
            logger.info("Добавлен новый маршрут: №{} ({} -> {}).",
                    tempRoute.getRouteNumber(), tempRoute.getDeparturePoint(), tempRoute.getDestinationPoint());
            loadData();
        } else {
            logger.debug("Добавление нового маршрута было отменено пользователем.");
        }
    }

    /**
     * Обработчик нажатия кнопки "Редактировать".
     * Открывает диалоговое окно для изменения данных выбранного маршрута.
     * Если маршрут не выбран, показывает предупреждение.
     */
    @FXML
    private void handleEdit() {
        Route selectedRoute = routeTable.getSelectionModel().getSelectedItem();
        if (selectedRoute != null) {
            boolean okClicked = showRouteEditDialog(selectedRoute);
            if (okClicked) {
                routeDao.update(selectedRoute);
                logger.info("Изменены данные маршрута с ID [{}]: №{} ({} -> {}).",
                        selectedRoute.getRouteId(), selectedRoute.getRouteNumber(),
                        selectedRoute.getDeparturePoint(), selectedRoute.getDestinationPoint());
                loadData();
            } else {
                logger.debug("Редактирование маршрута ID [{}] было отменено пользователем.", selectedRoute.getRouteId());
            }
        } else {
            logger.warn("Попытка редактирования: действие отменено, маршрут не выбран в таблице.");
            showAlert("Выберите маршрут в таблице для редактирования.");
        }
    }

    /**
     * Обработчик нажатия кнопки "Удалить".
     * Удаляет выбранный маршрут из базы данных.
     * Если маршрут не выбран, показывает предупреждение.
     * Обрабатывает исключения, связанные с ограничениями внешних ключей в БД.
     */
    @FXML
    private void handleDelete() {
        Route selectedRoute = routeTable.getSelectionModel().getSelectedItem();
        if (selectedRoute != null) {
            try {
                routeDao.delete(selectedRoute.getRouteId());
                logger.info("Из базы данных удален маршрут: №{} (ID: {}, {} -> {}).",
                        selectedRoute.getRouteNumber(), selectedRoute.getRouteId(),
                        selectedRoute.getDeparturePoint(), selectedRoute.getDestinationPoint());
                loadData();
            } catch (RuntimeException e) {
                logger.error("Не удалось удалить маршрут с ID [{}] из-за ограничений внешнего ключа в БД.", selectedRoute.getRouteId(), e);
                showAlert("Невозможно удалить маршрут: " + e.getMessage());
            }
        } else {
            logger.warn("Попытка удаления: действие отменено, маршрут не выбран в таблице.");
            showAlert("Выберите маршрут в таблице для удаления.");
        }
    }

    /**
     * Открывает модальное диалоговое окно для создания или редактирования данных маршрута.
     *
     * @param route объект маршрута с данными для отображения в диалоге (пустой для создания нового).
     * @return true, если пользователь нажал OK и сохранил изменения, false в противном случае.
     */
    private boolean showRouteEditDialog(Route route) {
        try {
            logger.debug("Загрузка FXML-формы route-edit-view.fxml для редактирования маршрута.");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/route-edit-view.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Редактирование маршрута");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            RouteEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setRoute(route);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            logger.error("Критическая ошибка ввода-вывода интерфейса при загрузке fxml-формы route-edit-view.fxml", e);
            return false;
        }
    }

    /**
     * Отображает модальное всплывающее окно с предупреждением для пользователя.
     *
     * @param content текстовое содержание предупреждения.
     */
    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}