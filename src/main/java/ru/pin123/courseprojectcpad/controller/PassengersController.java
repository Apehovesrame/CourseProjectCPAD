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
import ru.pin123.courseprojectcpad.dao.PassengerDaoImpl;
import ru.pin123.courseprojectcpad.model.Passenger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PassengersController implements Initializable {

    @FXML private TableView<Passenger> passengerTable;
    @FXML private TableColumn<Passenger, String> colLastName;
    @FXML private TableColumn<Passenger, String> colFirstName;
    @FXML private TableColumn<Passenger, String> colMiddleName;
    @FXML private TableColumn<Passenger, String> colPassport;

    private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();
    private final ObservableList<Passenger> passengerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colMiddleName.setCellValueFactory(new PropertyValueFactory<>("middleName"));
        colPassport.setCellValueFactory(new PropertyValueFactory<>("passport"));

        passengerTable.setItems(passengerList);
        loadData();
    }

    private void loadData() {
        passengerList.clear();
        passengerList.addAll(passengerDao.findAll());
    }

    @FXML
    private void handleAdd() {
        Passenger tempPassenger = new Passenger();
        boolean okClicked = showPassengerEditDialog(tempPassenger);
        if (okClicked) {
            passengerDao.save(tempPassenger);
            loadData();
        }
    }

    @FXML
    private void handleEdit() {
        Passenger selectedPassenger = passengerTable.getSelectionModel().getSelectedItem();
        if (selectedPassenger != null) {
            boolean okClicked = showPassengerEditDialog(selectedPassenger);
            if (okClicked) {
                passengerDao.update(selectedPassenger);
                loadData();
            }
        } else {
            showAlert("Выберите пассажира в таблице для редактирования.");
        }
    }

    @FXML
    private void handleDelete() {
        Passenger selectedPassenger = passengerTable.getSelectionModel().getSelectedItem();
        if (selectedPassenger != null) {
            try {
                passengerDao.delete(selectedPassenger.getPassengerId());
                loadData();
            } catch (RuntimeException e) {
                showAlert("Невозможно удалить пассажира: " + e.getMessage());
            }
        } else {
            showAlert("Выберите пассажира в таблице для удаления.");
        }
    }

    private boolean showPassengerEditDialog(Passenger passenger) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/passenger-edit-view.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Данные пассажира");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            PassengerEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPassenger(passenger);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}