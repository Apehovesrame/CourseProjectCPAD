package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pin123.courseprojectcpad.dao.PassengerDaoImpl;
import ru.pin123.courseprojectcpad.dao.StopDaoImpl;
import ru.pin123.courseprojectcpad.dao.TripDaoImpl;
import ru.pin123.courseprojectcpad.model.*;
import ru.pin123.courseprojectcpad.service.TicketingService;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TicketSellController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(TicketSellController.class);

    // Подключаем все необходимые DAO
    private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();
    private final TripDaoImpl tripDao = new TripDaoImpl();
    private final StopDaoImpl stopDao = new StopDaoImpl();
    private final TicketingService ticketingService = new TicketingService();

    @FXML private Button sellButton;
    @FXML private TextField birthYearField;
    @FXML private ListView<Trip> tripsListView;
    @FXML private ComboBox<StopItem> stopComboBox;
    @FXML private Label costLabel;
    @FXML private TextField fioField;
    @FXML private TextField passportField;
    @FXML private GridPane seatsGrid;

    private Integer selectedSeatNumber = null;
    private Button selectedSeatButton = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Загружаем все доступные рейсы из базы
        try {
            List<Trip> trips = tripDao.findAll();
            tripsListView.setItems(FXCollections.observableArrayList(trips));
        } catch (Exception e) {
            logger.error("Failed to load trips", e);
            showAlert(Alert.AlertType.ERROR, "Ошибка БД", "Не удалось загрузить рейсы");
        }

        // 2. Слушатель: когда кассир кликает на рейс
        tripsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) onTripSelected(newVal);
        });

        // 3. Слушатель: когда кассир выбирает остановку — меняем цену
        stopComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) costLabel.setText(newVal.getPrice().toString());
        });
    }

    private void onTripSelected(Trip trip) {
        // Загружаем реальные остановки из БД
        List<Stop> allStops = stopDao.findAll();
        List<StopItem> stopItems = new ArrayList<>();

        // Для примера генерируем стоимость от 150 руб. с шагом в 100 руб. за каждую следующую остановку
        BigDecimal basePrice = new BigDecimal("150.00");
        for (int i = 0; i < allStops.size(); i++) {
            BigDecimal price = basePrice.add(new BigDecimal(i * 100));
            stopItems.add(new StopItem(allStops.get(i), price));
        }

        stopComboBox.setItems(FXCollections.observableArrayList(stopItems));

        // Отрисовываем салон
        drawBusSeats(trip);
    }

    private void drawBusSeats(Trip trip) {
        seatsGrid.getChildren().clear();
        selectedSeatNumber = null;
        selectedSeatButton = null;

        if (trip.getBus() == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "К этому рейсу не привязан автобус!");
            return;
        }

        int capacity = trip.getBus().getSeatCapacity();
        List<Integer> occupiedSeats = ticketingService.getOccupiedSeats(trip.getTripId());
        int columns = 4; // 4 кресла в ряду

        for (int i = 1; i <= capacity; i++) {
            Button seatBtn = new Button(String.valueOf(i));
            seatBtn.setPrefSize(50, 50);

            if (occupiedSeats.contains(i)) {
                // Занято (Красный)
                seatBtn.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                seatBtn.setDisable(true);
            } else {
                // Свободно (Серый)
                seatBtn.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;");
                int currentSeat = i;
                seatBtn.setOnAction(e -> handleSeatSelection(currentSeat, seatBtn));
            }

            // Рассадка с проходом посередине
            int row = (i - 1) / columns;
            int col = (i - 1) % columns;
            if (col >= 2) col++; // Проход

            seatsGrid.add(seatBtn, col, row);
        }
    }

    private void handleSeatSelection(int seatNumber, Button seatBtn) {
        if (selectedSeatButton != null) {
            selectedSeatButton.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;");
        }
        selectedSeatNumber = seatNumber;
        selectedSeatButton = seatBtn;
        // Выбрано (Зеленый)
        seatBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    @FXML
    public void onSellTicketClick() {
        try {
            Trip selectedTrip = tripsListView.getSelectionModel().getSelectedItem();
            StopItem selectedStop = stopComboBox.getValue();
            String fio = fioField.getText().trim();
            String passport = passportField.getText().trim();
            String birthYearStr = birthYearField.getText().trim(); // <-- Новое поле

            if (selectedTrip == null || selectedStop == null || selectedSeatNumber == null ||
                    fio.isEmpty() || passport.isEmpty() || birthYearStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Внимание", "Заполните все поля и выберите место!");
                return;
            }

            int birthYear = Integer.parseInt(birthYearStr);

            String[] nameParts = fio.split("\\s+");
            String lastName = nameParts.length > 0 ? nameParts[0] : "-";
            String firstName = nameParts.length > 1 ? nameParts[1] : "-";
            String middleName = nameParts.length > 2 ? nameParts[2] : "";

            // 1. Создаем или получаем пассажира (теперь с годом рождения!)
            Passenger passenger = passengerDao.getOrCreate(lastName, firstName, middleName, passport, birthYear);

            BigDecimal cost = new BigDecimal(costLabel.getText());
            User currentUser = Session.getCurrentUser();

            // 2. Оформляем билет в базе
            Ticket newTicket = ticketingService.sellTicket(
                    selectedTrip, passenger, selectedStop.getStop(), currentUser, selectedSeatNumber, cost
            );

            // 3. ГЕНЕРАЦИЯ УНИКАЛЬНОГО РЕГИСТРАЦИОННОГО НОМЕРА (Требование ТЗ)
            // Формат: TKT-МесяцДень-СлучайныеЦифры (например: TKT-0606-4581)
            String regNumber = String.format("TKT-%02d%02d-%04d",
                    java.time.LocalDate.now().getMonthValue(),
                    java.time.LocalDate.now().getDayOfMonth(),
                    (int)(Math.random() * 10000));

            String saleDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String depDate = selectedTrip.getDepartureDatetime().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            // 4. ПОКАЗЫВАЕМ КРАСИВЫЙ ЧЕК
            showReceipt(regNumber, fio + " (Паспорт: " + passport + ")",
                    selectedTrip.getRoute().getDeparturePoint() + " - " + selectedTrip.getRoute().getDestinationPoint(),
                    selectedTrip.getBus().getModel() + " (" + selectedTrip.getBus().getLicensePlate() + ")",
                    String.valueOf(selectedSeatNumber), depDate, saleDate, cost.toString());

            // Сбрасываем форму
            fioField.clear();
            passportField.clear();
            birthYearField.clear();
            drawBusSeats(selectedTrip);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Год рождения должен быть числом!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
        }
    }

    // Метод для открытия окна чека
    private void showReceipt(String regNum, String pass, String route, String bus, String seat, String dep, String sale, String cost) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/ticket-receipt-view.fxml"));
            javafx.scene.layout.AnchorPane page = loader.load();

            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Маршрутная квитанция");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.setScene(new javafx.scene.Scene(page));

            TicketReceiptController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTicketData(regNum, pass, route, bus, seat, dep, sale, cost);

            dialogStage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

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