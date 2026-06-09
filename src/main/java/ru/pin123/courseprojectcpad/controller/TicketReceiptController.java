package ru.pin123.courseprojectcpad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер модального окна для отображения чека (квитанции) проданного билета.
 * Отвечает за визуализацию детальной информации о совершенной покупке
 * (регистрационный номер, пассажир, маршрут, место, стоимость и т.д.).
 */
public class TicketReceiptController {

    private static final Logger logger = LoggerFactory.getLogger(TicketReceiptController.class);

    @FXML private Label lblRegNumber;
    @FXML private Label lblPassenger;
    @FXML private Label lblRoute;
    @FXML private Label lblBus;
    @FXML private Label lblSeat;
    @FXML private Label lblDeparture;
    @FXML private Label lblSaleDate;
    @FXML private Label lblCost;

    // Внедряем ресурсы для локализации валюты
    @FXML private ResourceBundle resources;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setTicketData(String regNum, String passenger, String route, String bus,
                              String seat, String departure, String saleDate, String cost) {
        lblRegNumber.setText(regNum);
        lblPassenger.setText(passenger);
        lblRoute.setText(route);
        lblBus.setText(bus);
        lblSeat.setText(seat);
        lblDeparture.setText(departure);
        lblSaleDate.setText(saleDate);

        // ИСПРАВЛЕНО: Вместо "руб." берем валюту из файла перевода
        lblCost.setText(cost + " " + resources.getString("currency"));

        logger.info("Сформирован и отображен чек для билета. Рег. номер: {}, Пассажир: {}, Маршрут: {}, Стоимость: {} руб.",
                regNum, passenger, route, cost);
    }

    @FXML
    private void handleClose() {
        logger.debug("Окно с чеком билета закрыто пользователем.");
        dialogStage.close();
    }
}