package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.pin123.courseprojectcpad.dao.TripDaoImpl;
import ru.pin123.courseprojectcpad.model.Trip;
import java.net.URL;
import java.util.ResourceBundle;

public class TripsController implements Initializable {
    @FXML private TableView<Trip> tripTable;
    @FXML private TableColumn<Trip, String> colRoute;
    @FXML private TableColumn<Trip, String> colDeparture;
    @FXML private TableColumn<Trip, String> colArrival;
    @FXML private TableColumn<Trip, String> colBus;

    private final TripDaoImpl tripDao = new TripDaoImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departureDatetime"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalDatetime"));
        colBus.setCellValueFactory(new PropertyValueFactory<>("bus"));

        tripTable.setItems(FXCollections.observableArrayList(tripDao.findAll()));
    }

    @FXML
    public void handleCreateTrip() {
        // Логика открытия окна создания рейса
    }
}