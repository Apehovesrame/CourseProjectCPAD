package ru.pin123.courseprojectcpad.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class TicketSellController {

    public GridPane seatsGrid;
    @FXML private ComboBox<String> tripComboBox;
    @FXML private ComboBox<String> passengerComboBox;
    @FXML private ComboBox<String> stopComboBox;
    @FXML private TextField seatNumberField;
    @FXML private TextField costField;
    @FXML private Button sellButton;

    @FXML
    void onSellTicketClick(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Система");
        alert.setHeaderText(null);
        alert.setContentText("система продажи билета отрабатывает, дальше больше");
        alert.showAndWait();
    }
}