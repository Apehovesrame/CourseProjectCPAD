package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.pin123.courseprojectcpad.dao.DriverDaoImpl;
import ru.pin123.courseprojectcpad.model.Driver;

import java.net.URL;
import java.util.ResourceBundle;

public class DriversController implements Initializable {

    // Ссылки на элементы из FXML (fx:id должны совпадать!)
    @FXML private TableView<Driver> driverTable;
    @FXML private TableColumn<Driver, Long> colId;
    @FXML private TableColumn<Driver, String> colLastName;
    @FXML private TableColumn<Driver, String> colFirstName;
    @FXML private TableColumn<Driver, String> colMiddleName;

    private final DriverDaoImpl driverDao = new DriverDaoImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Настройка колонок таблицы
        colId.setCellValueFactory(new PropertyValueFactory<>("driverId"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colMiddleName.setCellValueFactory(new PropertyValueFactory<>("middleName"));

        // Загрузка данных
        loadDrivers();
    }

    private void loadDrivers() {
        driverTable.setItems(FXCollections.observableArrayList(driverDao.findAll()));
    }

    @FXML
    private void handleAdd() {
        // Здесь будет логика добавления (например, открытие нового окна)
    }

    @FXML
    private void handleDelete() {
        // Здесь будет логика удаления
    }
}