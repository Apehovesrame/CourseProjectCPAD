package ru.pin123.courseprojectcpad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер модального окна для отображения чека (квитанции) проданного билета.
 * Отвечает за визуализацию детальной информации о совершенной покупке
 * (регистрационный номер, пассажир, маршрут, место, стоимость и т.д.).
 */
public class TicketReceiptController {

    /** Логгер для фиксации событий отображения и закрытия чека. */
    private static final Logger logger = LoggerFactory.getLogger(TicketReceiptController.class);

    /** Метка для отображения регистрационного номера билета. */
    @FXML private Label lblRegNumber;
    /** Метка для отображения ФИО пассажира. */
    @FXML private Label lblPassenger;
    /** Метка для отображения названия маршрута. */
    @FXML private Label lblRoute;
    /** Метка для отображения номера/модели автобуса. */
    @FXML private Label lblBus;
    /** Метка для отображения номера места. */
    @FXML private Label lblSeat;
    /** Метка для отображения даты и времени отправления. */
    @FXML private Label lblDeparture;
    /** Метка для отображения даты и времени продажи билета. */
    @FXML private Label lblSaleDate;
    /** Метка для отображения итоговой стоимости билета. */
    @FXML private Label lblCost;

    /** Ссылка на модальное окно диалога. */
    private Stage dialogStage;

    /**
     * Устанавливает ссылку на модальное окно диалога.
     * @param dialogStage объект Stage для управления окном.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Заполняет элементы интерфейса данными о проданном билете для отображения в чеке.
     *
     * @param regNum    регистрационный номер билета.
     * @param passenger ФИО пассажира.
     * @param route     название маршрута.
     * @param bus       информация об автобусе.
     * @param seat      номер места.
     * @param departure дата и время отправления.
     * @param saleDate  дата и время продажи.
     * @param cost      стоимость билета.
     */
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

        logger.info("Сформирован и отображен чек для билета. Рег. номер: {}, Пассажир: {}, Маршрут: {}, Стоимость: {} руб.",
                regNum, passenger, route, cost);
    }

    /**
     * Обрабатывает нажатие кнопки закрытия. Закрывает модальное окно с чеком.
     */
    @FXML
    private void handleClose() {
        logger.debug("Окно с чеком билета закрыто пользователем.");
        dialogStage.close();
    }
}