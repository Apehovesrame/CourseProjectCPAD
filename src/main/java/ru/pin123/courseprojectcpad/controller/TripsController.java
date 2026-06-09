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

public class TripsController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(TripsController.class);

    @FXML private TableView<Trip> tripTable;
    @FXML private TableColumn<Trip, String> colRoute;
    @FXML private TableColumn<Trip, String> colDeparture;
    @FXML private TableColumn<Trip, String> colArrival;
    @FXML private TableColumn<Trip, String> colBus;

    // Внедряем ресурсы локализации
    @FXML private ResourceBundle resources;

    private final TripDaoImpl tripDao = new TripDaoImpl();
    private final ObservableList<Trip> tripList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Сохраняем внедренный бандл
        this.resources = resources;

        logger.info("Инициализация контроллера рейсов. Настройка колонок таблицы.");

        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colBus.setCellValueFactory(new PropertyValueFactory<>("bus"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        colDeparture.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDepartureDatetime().format(formatter)));
        colArrival.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getArrivalDatetime().format(formatter)));

        tripTable.setItems(tripList);
        loadData();
    }

    private void loadData() {
        try {
            tripList.clear();
            tripList.addAll(tripDao.findAll());
            logger.info("Успешно загружено {} записей рейсов из базы данных.", tripList.size());
        } catch (Exception e) {
            logger.error("Критическая ошибка при попытке загрузки списка рейсов из слоя DAO", e);
        }
    }

    @FXML
    public void handleCreateTrip() {
        logger.debug("Открытие диалога для назначения нового рейса.");
        Trip newTrip = new Trip();
        showTripEditDialog(newTrip);
    }

    private void showTripEditDialog(Trip trip) {
        try {
            // ИСПРАВЛЕНО: Пробрасываем resources в окно редактирования
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/trip-edit-view.fxml"), resources);
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            // ИСПРАВЛЕНО: Динамический локализованный заголовок окна
            String titleKey = trip.getTripId() == null ? "trips.edit.title_assign" : "trips.edit.title_edit";
            dialogStage.setTitle(resources.getString(titleKey));

            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            TripEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTrip(trip);

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                logger.info("Данные рейса успешно сохранены через диалоговое окно. Обновление таблицы.");
                loadData();
            } else {
                logger.debug("Диалоговое окно рейса закрыто без сохранения изменений.");
            }
        } catch (IOException e) {
            logger.error("Критическая ошибка ввода-вывода интерфейса при загрузке fxml-формы trip-edit-view.fxml", e);
        }
    }

    @FXML
    private void handleEditTrip() {
        Trip selectedTrip = tripTable.getSelectionModel().getSelectedItem();
        if (selectedTrip != null) {
            logger.debug("Открытие диалога для редактирования рейса ID [{}].", selectedTrip.getTripId());
            showTripEditDialog(selectedTrip);
        } else {
            logger.warn("Попытка редактирования: действие отменено, рейс не выбран в таблице.");
            // ИСПРАВЛЕНО: Локализация предупреждения
            showAlert(resources.getString("alert.select_item"));
        }
    }

    @FXML
    private void handleDeleteTrip() {
        Trip selectedTrip = tripTable.getSelectionModel().getSelectedItem();
        if (selectedTrip != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            // ИСПРАВЛЕНО: Локализация окна подтверждения удаления
            confirm.setTitle(resources.getString("alert.confirm.title"));
            confirm.setHeaderText(resources.getString("trips.delete.header"));
            confirm.setContentText(resources.getString("trips.delete.content"));

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    tripDao.delete(selectedTrip.getTripId());
                    logger.info("Из базы данных успешно удален рейс ID [{}].", selectedTrip.getTripId());
                    loadData();
                } catch (Exception e) {
                    logger.error("Ошибка при удалении рейса ID [{}] из базы данных.", selectedTrip.getTripId(), e);
                    // ИСПРАВЛЕНО: Локализация ошибки
                    showAlert(resources.getString("alert.error.title") + ": " + e.getMessage());
                }
            } else {
                logger.debug("Удаление рейса ID [{}] отменено пользователем.", selectedTrip.getTripId());
            }
        } else {
            logger.warn("Попытка удаления: действие отменено, рейс не выбран в таблице.");
            // ИСПРАВЛЕНО: Локализация предупреждения
            showAlert(resources.getString("alert.select_item"));
        }
    }

    private void showAlert(String content) {
        // ИСПРАВЛЕНО: Добавлен локализованный заголовок для предупреждений
        Alert alert = new Alert(Alert.AlertType.WARNING, content);
        alert.setTitle(resources.getString("alert.warning.title"));
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}