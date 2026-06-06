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
import ru.pin123.courseprojectcpad.dao.DriverDaoImpl;
import ru.pin123.courseprojectcpad.model.Driver;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DriversController implements Initializable {

    @FXML private TableView<Driver> driverTable;
    @FXML private TableColumn<Driver, String> colLastName;
    @FXML private TableColumn<Driver, String> colFirstName;
    @FXML private TableColumn<Driver, String> colMiddleName;
    @FXML private TableColumn<Driver, Integer> colAge;
    @FXML private TableColumn<Driver, String> colPassport;

    private final DriverDaoImpl driverDao = new DriverDaoImpl();
    private final ObservableList<Driver> driverList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Привязываем колонки к полям модели Driver
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colMiddleName.setCellValueFactory(new PropertyValueFactory<>("middleName"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colPassport.setCellValueFactory(new PropertyValueFactory<>("passport"));

        driverTable.setItems(driverList);
        loadData();
    }

    private void loadData() {
        driverList.clear();
        driverList.addAll(driverDao.findAll());
    }

    @FXML
    private void handleAdd() {
        Driver tempDriver = new Driver();
        boolean okClicked = showDriverEditDialog(tempDriver);
        if (okClicked) {
            driverDao.save(tempDriver);
            loadData();
        }
    }

    @FXML
    private void handleEdit() {
        Driver selectedDriver = driverTable.getSelectionModel().getSelectedItem();
        if (selectedDriver != null) {
            boolean okClicked = showDriverEditDialog(selectedDriver);
            if (okClicked) {
                driverDao.update(selectedDriver);
                loadData();
            }
        } else {
            showAlert("Выберите водителя в таблице для редактирования.");
        }
    }

    @FXML
    private void handleDelete() {
        Driver selectedDriver = driverTable.getSelectionModel().getSelectedItem();
        if (selectedDriver != null) {
            try {
                driverDao.delete(selectedDriver.getDriverId());
                loadData();
            } catch (RuntimeException e) {
                showAlert("Невозможно удалить водителя: " + e.getMessage());
            }
        } else {
            showAlert("Выберите водителя в таблице для удаления.");
        }
    }

    private boolean showDriverEditDialog(Driver driver) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/driver-edit-view.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Данные водителя");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            DriverEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setDriver(driver);

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