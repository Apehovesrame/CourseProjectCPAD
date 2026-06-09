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

    private static final Logger logger = LoggerFactory.getLogger(RoutesController.class);

    @FXML private TableView<Route> routeTable;
    @FXML private TableColumn<Route, String> colNumber;
    @FXML private TableColumn<Route, String> colFrom;
    @FXML private TableColumn<Route, String> colTo;
    @FXML private TableColumn<Route, String> colDuration;

    // Внедряем файл локализации
    @FXML private ResourceBundle resources;

    private final RouteDaoImpl routeDao = new RouteDaoImpl();
    private final ObservableList<Route> routeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Сохраняем внедренный JavaFX бандл ресурсов
        this.resources = resources;

        colNumber.setCellValueFactory(new PropertyValueFactory<>("routeNumber"));
        colFrom.setCellValueFactory(new PropertyValueFactory<>("departurePoint"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("destinationPoint"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));

        routeTable.setItems(routeList);
        loadData();
    }

    private void loadData() {
        try {
            routeList.clear();
            routeList.addAll(routeDao.findAll());
            logger.info("Успешно загружено {} записей маршрутов из базы данных.", routeList.size());
        } catch (Exception e) {
            logger.error("Критическая ошибка при попытке загрузки списка маршрутов из слоя DAO", e);
        }
    }

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
            // ИСПРАВЛЕНО: Локализация предупреждения
            showAlert(resources.getString("alert.select_item"));
        }
    }

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
                // ИСПРАВЛЕНО: Локализация ошибки удаления
                showAlert(resources.getString("routes.delete_error") + ": " + e.getMessage());
            }
        } else {
            logger.warn("Попытка удаления: действие отменено, маршрут не выбран в таблице.");
            // ИСПРАВЛЕНО: Локализация предупреждения
            showAlert(resources.getString("alert.select_item"));
        }
    }

    private boolean showRouteEditDialog(Route route) {
        try {
            logger.debug("Загрузка FXML-формы route-edit-view.fxml для редактирования маршрута.");
            // ИСПРАВЛЕНО: Пробрасываем resources в загрузчик окна
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/route-edit-view.fxml"), resources);
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            // ИСПРАВЛЕНО: Берем заголовок из ресурсов
            dialogStage.setTitle(resources.getString("routes.edit.title"));
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

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        // ИСПРАВЛЕНО: Заголовок алерта
        alert.setTitle(resources.getString("alert.warning.title"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}