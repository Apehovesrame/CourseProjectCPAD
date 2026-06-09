package ru.pin123.courseprojectcpad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketReceiptController {

    @FXML private Label lblRegNumber;
    @FXML private Label lblPassenger;
    @FXML private Label lblRoute;
    @FXML private Label lblBus;
    @FXML private Label lblSeat;
    @FXML private Label lblDeparture;
    @FXML private Label lblSaleDate;
    @FXML private Label lblCost;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Метод для заполнения чека
    public void setTicketData(String regNum, String passenger, String route, String bus,
                              String seat, String departure, String saleDate, String cost) {
        lblRegNumber.setText(regNum);
        lblPassenger.setText(passenger);
        lblRoute.setText(route);
        lblBus.setText(bus);
        lblSeat.setText(seat);
        lblDeparture.setText(departure);
        lblSaleDate.setText(saleDate);
        lblCost.setText(cost + " руб.");
    }

    @FXML
    private void handleClose() {
        dialogStage.close();
    }
}