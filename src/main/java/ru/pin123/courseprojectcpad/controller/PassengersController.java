package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.pin123.courseprojectcpad.dao.PassengerDaoImpl;
import ru.pin123.courseprojectcpad.model.Passenger;
import java.net.URL;
import java.util.ResourceBundle;

public class PassengersController implements Initializable {
    @FXML private TableView<Passenger> passengerTable;
    @FXML private TableColumn<Passenger, Long> colId;
    @FXML private TableColumn<Passenger, String> colFio;
    @FXML private TableColumn<Passenger, String> colPassport;

    private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("passengerId"));
        colPassport.setCellValueFactory(new PropertyValueFactory<>("passportNumber"));

        // Объединяем ФИО для отображения
        colFio.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.concat(
                        cellData.getValue().getLastName(), " ",
                        cellData.getValue().getFirstName(), " ",
                        cellData.getValue().getMiddleName()
                )
        );

        passengerTable.setItems(FXCollections.observableArrayList(passengerDao.findAll()));
    }
}