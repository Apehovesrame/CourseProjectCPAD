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

public class TripsController implements Initializable {
    @FXML private TableView<Trip> tripTable;
    @FXML private TableColumn<Trip, String> colRoute;
    @FXML private TableColumn<Trip, String> colDeparture;
    @FXML private TableColumn<Trip, String> colArrival;
    @FXML private TableColumn<Trip, String> colBus;

    private final TripDaoImpl tripDao = new TripDaoImpl();
    private final ObservableList<Trip> tripList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    private void loadData() {
        tripList.clear();
        tripList.addAll(tripDao.findAll());
    }

    @FXML
    public void handleCreateTrip() {
        Trip newTrip = new Trip();
        showTripEditDialog(newTrip);
    }

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
                loadData(); // Обновляем таблицу, если нажали "Сохранить"
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditTrip() {
        Trip selectedTrip = tripTable.getSelectionModel().getSelectedItem();
        if (selectedTrip != null) {
            showTripEditDialog(selectedTrip);
        } else {
            showAlert("Выберите рейс в таблице для редактирования.");
        }
    }

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
                    loadData(); // Обновляем таблицу
                } catch (Exception e) {
                    showAlert("Ошибка при удалении: " + e.getMessage());
                }
            }
        } else {
            showAlert("Выберите рейс в таблице для удаления.");
        }
    }

    private void showAlert(String content) {
        new Alert(Alert.AlertType.WARNING, content).showAndWait();
    }
}