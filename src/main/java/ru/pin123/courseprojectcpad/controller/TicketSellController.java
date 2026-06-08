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
    @FXML private Label lblDeparturePoint;
    @FXML private Label lblDestinationPoint;
    @FXML private Label lblDuration;

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
        // Сбрасываем поля, если рейс не выбран
        if (trip == null || trip.getRoute() == null) {
            lblDeparturePoint.setText("—");
            lblDestinationPoint.setText("—");
            lblDuration.setText("—");
            stopComboBox.getItems().clear();
            costLabel.setText("0.00");
            seatsGrid.getChildren().clear();
            return;
        }

        // 1. Получаем данные маршрута и обновляем интерфейс
        Route route = trip.getRoute();
        lblDeparturePoint.setText(route.getDeparturePoint());
        lblDestinationPoint.setText(route.getDestinationPoint());

        // В вашем классе Route есть удобный метод getFormattedDuration(), который возвращает строку вида "6 ч 0 мин"
        lblDuration.setText(route.getFormattedDuration());

        // 2. Загружаем реальные остановки из БД (ваш существующий код)
        List<Stop> allStops = stopDao.findAll();
        List<StopItem> stopItems = new ArrayList<>();
        BigDecimal basePrice = new BigDecimal("150.00"); // Базовая цена

        for (int i = 0; i < allStops.size(); i++) {
            BigDecimal price = basePrice.add(new BigDecimal(i * 100));
            stopItems.add(new StopItem(allStops.get(i), price));
        }
        stopComboBox.setItems(FXCollections.observableArrayList(stopItems));

        // 3. Отрисовываем салон (ваш существующий код)
        drawBusSeats(trip);
    }

    private void drawBusSeats(Trip trip) {
        seatsGrid.getChildren().clear();
        seatsGrid.getColumnConstraints().clear(); // Важно: очищаем старые настройки колонок
        selectedSeatNumber = null;
        selectedSeatButton = null;

        if (trip.getBus() == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "К этому рейсу не привязан автобус!");
            return;
        }

        int capacity = trip.getBus().getSeatCapacity();
        List<Integer> occupiedSeats = ticketingService.getOccupiedSeats(trip.getTripId());
        int seatsPerRow = 4; // Всего мест в ряду (2 слева + 2 справа)

        // 1. Настраиваем 5 колонок для GridPane (индексы: 0, 1, 2-проход, 3, 4)
        for (int c = 0; c < 5; c++) {
            // Используем полный путь к классу, чтобы не добавлять импорты вручную
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            if (c == 2) {
                // Это колонка прохода
                cc.setMinWidth(15);
                cc.setPrefWidth(15);
            } else {
                // Это колонки для кресел
                int buttonSize = 30;
                cc.setMinWidth(buttonSize);
                cc.setPrefWidth(buttonSize);
                cc.setMaxWidth(buttonSize);
            }
            cc.setHalignment(javafx.geometry.HPos.CENTER);
            seatsGrid.getColumnConstraints().add(cc);
        }

        // 2. Добавляем кнопки мест
        for (int i = 1; i <= capacity; i++) {
            Button seatBtn = new Button(String.valueOf(i));
            seatBtn.setMinSize(30, 30);
            seatBtn.setPrefSize(30, 30);
            seatBtn.setMaxSize(30, 30);

            if (occupiedSeats.contains(i)) {
                seatBtn.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                seatBtn.setDisable(true);
            } else {
                seatBtn.setStyle("-fx-background-color: #e0e0e0; -fx-cursor: hand;");
                int currentSeat = i;
                seatBtn.setOnAction(e -> handleSeatSelection(currentSeat, seatBtn));
            }

            // Рассчитываем row и col
            int row = (i - 1) / seatsPerRow;
            int col = (i - 1) % seatsPerRow;

            // Сдвигаем колонку, если это правая сторона (3-е и 4-е место в ряду)
            if (col >= 2) {
                col += 1; // Пропускаем колонку 2 (проход)
            }

            javafx.scene.layout.GridPane.setHgrow(seatBtn, javafx.scene.layout.Priority.NEVER);
            javafx.scene.layout.GridPane.setVgrow(seatBtn, javafx.scene.layout.Priority.NEVER);

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