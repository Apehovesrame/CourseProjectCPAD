package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ru.pin123.courseprojectcpad.dao.StopDaoImpl;
import ru.pin123.courseprojectcpad.model.*;
import ru.pin123.courseprojectcpad.service.TicketingService;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TicketSellController implements Initializable {

    // --- Элементы интерфейса из вашего FXML файла ---
    @FXML private ComboBox<Trip> comboTrips;
    @FXML private ComboBox<Passenger> comboPassengers;
    @FXML private ComboBox<Stop> comboStops;
    @FXML private TextField txtSeatNumber;
    @FXML private TextField txtCost;

    private final TicketingService ticketingService = new TicketingService();
    private final StopDaoImpl stopDao = new StopDaoImpl();

    // Временная заглушка: текущий авторизованный кассир
    private User currentUser = new User(1L, "Кассир", "Тестовый");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Загружаем в ComboBox список остановок при инициализации формы
        try {
            List<Stop> stops = stopDao.findAll();
            comboStops.setItems(FXCollections.observableArrayList(stops));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка БД", "Не удалось загрузить список остановок: " + e.getMessage());
        }
    }

    /**
     * Этот метод срабатывает при нажатии на кнопку "Оформить билет"
     */
    @FXML
    public void onSellTicketClick(ActionEvent event) {
        try {
            Trip selectedTrip = comboTrips.getValue();
            Passenger selectedPassenger = comboPassengers.getValue();
            Stop selectedStop = comboStops.getValue();

            if (selectedTrip == null || selectedPassenger == null || txtSeatNumber.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Внимание", "Пожалуйста, заполните все поля!");
                return;
            }

            int seatNumber = Integer.parseInt(txtSeatNumber.getText());
            BigDecimal cost = new BigDecimal(txtCost.getText());

            Ticket newTicket = ticketingService.sellTicket(
                    selectedTrip,
                    selectedPassenger,
                    selectedStop,
                    currentUser,
                    seatNumber,
                    cost
            );

            showAlert(Alert.AlertType.INFORMATION, "Успех!", "Билет №" + newTicket.getTicketId() + " успешно оформлен!");
            txtSeatNumber.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Номер места и цена должны быть числами.");
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка оформления", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}