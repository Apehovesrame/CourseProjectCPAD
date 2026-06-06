package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pin123.courseprojectcpad.model.*;
import ru.pin123.courseprojectcpad.service.TicketingService;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import ru.pin123.courseprojectcpad.dao.PassengerDaoImpl;

public class TicketSellController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(TicketSellController.class);
    private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();

    // --- Привязка к вашим fx:id из FXML ---
    @FXML private ListView<Trip> tripsListView;
    @FXML private ComboBox<StopItem> stopComboBox;
    @FXML private Label costLabel;
    @FXML private TextField fioField;
    @FXML private TextField passportField;
    @FXML private GridPane seatsGrid;

    private final TicketingService ticketingService = new TicketingService();
    // В будущем здесь понадобится PassengerDao для создания пассажира "на лету"
    // private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();

    // Переменная для хранения выбранного места (какую кнопку нажал кассир)
    private Integer selectedSeatNumber = null;
    private Button selectedSeatButton = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Загружаем все доступные рейсы в ListView
        try {
            //TODO: implement find all trips
            List<Trip> trips = List.of(); //tripDao.findAll(); // Пока используем findAll, потом можно сделать findActiveTrips
            tripsListView.setItems(FXCollections.observableArrayList(trips));
        } catch (Exception e) {
            logger.error("Failed to load trips", e);
        }

        // 2. Слушатель: когда кассир кликает на рейс в списке
        tripsListView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                onTripSelected(newValue);
            }
        });

        // 3. Слушатель: когда кассир выбирает остановку — меняем цену
        stopComboBox.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                costLabel.setText(newValue.getPrice().toString());
            }
        });
    }

    /**
     * Метод срабатывает при выборе рейса в списке
     */
    private void onTripSelected(Trip trip) {
        // 1. Загружаем остановки именно для этого рейса (заглушка, тут нужен метод DAO, который вернет цену до остановки)
        // Временно создаем список вручную для демонстрации
        stopComboBox.setItems(FXCollections.observableArrayList(
                new StopItem(new Stop(1L, "Владимир"), new BigDecimal("500.00")),
                new StopItem(new Stop(2L, "Москва"), new BigDecimal("1200.00"))
        ));

        // 2. Отрисовываем салон автобуса
        drawBusSeats(trip);
    }

    /**
     * Динамически генерирует кнопки посадочных мест в салоне (GridPane)
     */
    private void drawBusSeats(Trip trip) {
        seatsGrid.getChildren().clear(); // Очищаем старые кнопки
        selectedSeatNumber = null;
        selectedSeatButton = null;

        // Если у рейса нет автобуса, прерываем
        if (trip.getBus() == null) return;

        int capacity = trip.getBus().getSeatCapacity();

        // Получаем из нашего сервиса список уже проданных мест на этот рейс
        List<Integer> occupiedSeats = ticketingService.getOccupiedSeats(trip.getTripId());

        int columns = 4; // По 4 кресла в ряду

        for (int i = 1; i <= capacity; i++) {
            Button seatBtn = new Button(String.valueOf(i));
            seatBtn.setPrefSize(50, 50);

            if (occupiedSeats.contains(i)) {
                // Если место занято — красим в красный и блокируем
                seatBtn.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                seatBtn.setDisable(true);
            } else {
                // Если место свободно — стандартный цвет
                seatBtn.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;");
                int currentSeat = i;

                // При клике на свободное место
                seatBtn.setOnAction(_ -> handleSeatSelection(currentSeat, seatBtn));
            }

            // Математика рассадки: ряды и проход (gap) посередине
            int row = (i - 1) / columns;
            int col = (i - 1) % columns;
            if (col >= 2) col++; // Делаем "проход" между 2 и 3 сиденьями

            seatsGrid.add(seatBtn, col, row);
        }
    }

    /**
     * Обработка выбора места
     */
    private void handleSeatSelection(int seatNumber, Button seatBtn) {
        // Сбрасываем цвет предыдущего выбранного места
        if (selectedSeatButton != null) {
            selectedSeatButton.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;");
        }

        // Запоминаем новое и красим в зеленый
        selectedSeatNumber = seatNumber;
        selectedSeatButton = seatBtn;
        seatBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    @FXML
    public void onSellTicketClick() {
        try {
            Trip selectedTrip = tripsListView.getSelectionModel().getSelectedItem();
            StopItem selectedStop = stopComboBox.getValue();
            String fio = fioField.getText().trim();
            String passport = passportField.getText().trim();

            if (selectedTrip == null || selectedStop == null || selectedSeatNumber == null || fio.isEmpty() || passport.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Внимание", "Пожалуйста, заполните все поля и выберите место на схеме!");
                return;
            }

            // Разбиваем ФИО на Фамилию, Имя, Отчество (по логике вашего старого C# кода)
            String[] nameParts = fio.split("\\s+");
            String lastName = nameParts.length > 0 ? nameParts[0] : "-";
            String firstName = nameParts.length > 1 ? nameParts[1] : "-";
            String middleName = nameParts.length > 2 ? nameParts[2] : "";

            // TODO: Здесь должен быть вызов PassengerDao для получения или создания пассажира
            // Passenger passenger = passengerDao.getOrCreate(lastName, firstName, middleName, passport);
            Passenger passenger = passengerDao.getOrCreate(lastName, firstName, middleName, passport);

            BigDecimal cost = new BigDecimal(costLabel.getText());
            User currentUser = new User(); //Session.getCurrentUser(); // Берем реального кассира из сессии

            Ticket newTicket = ticketingService.sellTicket(
                    selectedTrip,
                    passenger,
                    selectedStop.getStop(),
                    currentUser,
                    selectedSeatNumber,
                    cost
            );

            showAlert(Alert.AlertType.INFORMATION, "Успех!", "Билет №" + newTicket.getTicketId() + " успешно оформлен!");

            // Сбрасываем форму и перерисовываем салон (чтобы только что купленное место стало красным)
            fioField.clear();
            passportField.clear();
            drawBusSeats(selectedTrip);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Некорректный формат цены.");
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

    // --- Вспомогательный DTO-класс для выпадающего списка остановок ---
    // (Аналог класса StopItem из вашего файла TicketSaleForm.cs)
    public static class StopItem {
        private final Stop stop;
        private final BigDecimal price;

        public StopItem(Stop stop, BigDecimal price) {
            this.stop = stop;
            this.price = price;
        }

        public Stop getStop() { return stop; }
        public BigDecimal getPrice() { return price; }

        @Override
        public String toString() {
            return stop.getName() + " — " + price + " руб.";
        }
    }
}