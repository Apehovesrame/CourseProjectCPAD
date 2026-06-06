package ru.pin123.courseprojectcpad.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.pin123.courseprojectcpad.model.*;
import ru.pin123.courseprojectcpad.service.TicketingService;

import java.math.BigDecimal;

public class TicketSellController {

    // --- Элементы интерфейса из вашего FXML файла ---
    @FXML private ComboBox<Trip> comboTrips;          // Выпадающий список рейсов
    @FXML private ComboBox<Passenger> comboPassengers;// Выпадающий список пассажиров
    @FXML private ComboBox<Stop> comboStops;          // Выпадающий список остановок
    @FXML private TextField txtSeatNumber;            // Поле для ввода номера места
    @FXML private TextField txtCost;                  // Поле для ввода цены

    // Подключаем наш сервис
    private final TicketingService ticketingService = new TicketingService();

    // Временная заглушка: текущий авторизованный кассир (потом будем брать из Login-окна)
    private User currentUser = new User(1L, "Кассир", "Тестовый");

    // Загружаем в ComboBox список остановок, до куда едет пассажир
    List<Stop> stops = new StopDaoImpl().findAll();
    comboStops.setItems(FXCollections.observableArrayList(stops));
    /**
     * Этот метод срабатывает при нажатии на кнопку "Оформить билет"
     */
    @FXML
    public void onSellTicketClick(ActionEvent event) {
        try {
            // 1. Собираем данные с формочки
            Trip selectedTrip = comboTrips.getValue();
            Passenger selectedPassenger = comboPassengers.getValue();
            Stop selectedStop = comboStops.getValue();

            // Проверяем, что поля не пустые
            if (selectedTrip == null || selectedPassenger == null || txtSeatNumber.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Внимание", "Пожалуйста, заполните все поля!");
                return;
            }

            int seatNumber = Integer.parseInt(txtSeatNumber.getText());
            BigDecimal cost = new BigDecimal(txtCost.getText());

            // 2. Отправляем в наш сервис! Никакого SQL здесь нет, контроллер чистый.
            Ticket newTicket = ticketingService.sellTicket(
                    selectedTrip,
                    selectedPassenger,
                    selectedStop,
                    currentUser,
                    seatNumber,
                    cost
            );

            // 3. Если всё прошло успешно (база не ругнулась, места есть) — радуем пользователя
            showAlert(Alert.AlertType.INFORMATION, "Успех!", "Билет №" + newTicket.getTicketId() + " успешно оформлен!");

            // Здесь можно очистить поля после продажи
            txtSeatNumber.clear();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Номер места и цена должны быть числами.");
        } catch (RuntimeException e) {
            // Если место занято или произошла ошибка в базе, сервис выкинет ошибку, и мы её покажем
            showAlert(Alert.AlertType.ERROR, "Ошибка оформления", e.getMessage());
        }
    }

    /**
     * Вспомогательный метод для красивого вывода всплывающих окошек
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}