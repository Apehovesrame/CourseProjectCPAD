package ru.pin123.courseprojectcpad.controller;

import javafx.application.Platform;
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

    private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();
    private final TripDaoImpl tripDao = new TripDaoImpl();
    private final StopDaoImpl stopDao = new StopDaoImpl();
    private final TicketingService ticketingService = new TicketingService();

    @FXML private Button sellButton;
    @FXML private ListView<Trip> tripsListView;
    @FXML private ComboBox<StopItem> stopComboBox;
    @FXML private Label costLabel;
    @FXML private GridPane seatsGrid;
    @FXML private Label lblDeparturePoint;
    @FXML private Label lblDestinationPoint;
    @FXML private Label lblDuration;

    // НОВЫЕ ПОЛЯ (Разделенное ФИО и Паспорт)
    @FXML private TextField tfLastName;
    @FXML private TextField tfFirstName;
    @FXML private TextField tfMiddleName;
    @FXML private TextField tfPassport;
    @FXML private TextField birthYearField;

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

        // 2. Слушатели кликов
        tripsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) onTripSelected(newVal);
        });

        stopComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) costLabel.setText(newVal.getPrice().toString());
        });

        // 3. Подключаем умную маску для паспорта (Авто-пробел и лимит 10 цифр)
        setupPassportMask(tfPassport);
    }

    // МЕТОД ДЛЯ МАСКИ ПАСПОРТА
    private void setupPassportMask(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            // Оставляем только цифры
            String digits = newValue.replaceAll("[^\\d]", "");

            // Не больше 10 цифр
            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }

            // Вставляем пробел после 4-й цифры
            StringBuilder formatted = new StringBuilder(digits);
            if (formatted.length() > 4) {
                formatted.insert(4, " ");
            }

            // Чтобы курсор не прыгал в начало, используем Platform.runLater
            if (!newValue.equals(formatted.toString())) {
                textField.setText(formatted.toString());
                Platform.runLater(textField::end);
            }
        });
    }

    private void onTripSelected(Trip trip) {
        try {
            if (trip == null || trip.getRoute() == null) {
                lblDeparturePoint.setText("—");
                lblDestinationPoint.setText("—");
                lblDuration.setText("—");
                stopComboBox.getItems().clear();
                costLabel.setText("0.00");
                seatsGrid.getChildren().clear();
                return;
            }

            Route route = trip.getRoute();
            lblDeparturePoint.setText(route.getDeparturePoint());
            lblDestinationPoint.setText(route.getDestinationPoint());
            lblDuration.setText(route.getFormattedDuration());

            List<Stop> allStops = stopDao.findAll();
            List<StopItem> stopItems = new ArrayList<>();
            BigDecimal basePrice = new BigDecimal("150.00");

            for (int i = 0; i < allStops.size(); i++) {
                BigDecimal price = basePrice.add(new BigDecimal(i * 100));
                stopItems.add(new StopItem(allStops.get(i), price));
            }
            stopComboBox.setItems(FXCollections.observableArrayList(stopItems));

            drawBusSeats(trip);

        } catch (Exception e) {
            logger.error("Ошибка при выборе рейса", e);
            showAlert(Alert.AlertType.ERROR, "Ошибка загрузки", "Не удалось загрузить данные рейса или остановок:\n" + e.getMessage());
        }
    }

    private void drawBusSeats(Trip trip) {
        seatsGrid.getChildren().clear();
        seatsGrid.getColumnConstraints().clear();
        selectedSeatNumber = null;
        selectedSeatButton = null;

        if (trip.getBus() == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "К этому рейсу не привязан автобус!");
            return;
        }

        int capacity = trip.getBus().getSeatCapacity();
        List<Integer> occupiedSeats = ticketingService.getOccupiedSeats(trip.getTripId());
        int seatsPerRow = 4;

        for (int c = 0; c < 5; c++) {
            javafx.scene.layout.ColumnConstraints cc = new javafx.scene.layout.ColumnConstraints();
            if (c == 2) {
                cc.setMinWidth(15);
                cc.setPrefWidth(15);
            } else {
                cc.setMinWidth(30);
                cc.setPrefWidth(30);
                cc.setMaxWidth(30);
            }
            cc.setHalignment(javafx.geometry.HPos.CENTER);
            seatsGrid.getColumnConstraints().add(cc);
        }

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

            int row = (i - 1) / seatsPerRow;
            int col = (i - 1) % seatsPerRow;
            if (col >= 2) col += 1;

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
        seatBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    @FXML
    public void onSellTicketClick() {
        try {
            Trip selectedTrip = tripsListView.getSelectionModel().getSelectedItem();
            StopItem selectedStop = stopComboBox.getValue();

            // Читаем новые разделенные поля
            String lastName = tfLastName.getText() != null ? tfLastName.getText().trim() : "";
            String firstName = tfFirstName.getText() != null ? tfFirstName.getText().trim() : "";
            String middleName = tfMiddleName.getText() != null ? tfMiddleName.getText().trim() : "";
            String passport = tfPassport.getText() != null ? tfPassport.getText().trim() : "";
            String birthYearStr = birthYearField.getText() != null ? birthYearField.getText().trim() : "";

            // БАЗОВАЯ ПРОВЕРКА НА ПУСТОТУ
            if (selectedTrip == null || selectedStop == null || selectedSeatNumber == null ||
                    lastName.isEmpty() || firstName.isEmpty() || passport.isEmpty() || birthYearStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Внимание", "Заполните обязательные поля и выберите место!");
                return;
            }

            // ЖЕСТКАЯ ВАЛИДАЦИЯ ФИО (Кириллица + Заглавная буква. Разрешаем двойные фамилии через дефис)
            if (!lastName.matches("^[А-ЯЁ][а-яё]*(-[А-ЯЁ][а-яё]*)?$") || !firstName.matches("^[А-ЯЁ][а-яё]*$")) {
                showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Фамилия и Имя должны быть на кириллице и начинаться с заглавной буквы!");
                return;
            }
            if (!middleName.isEmpty() && !middleName.matches("^[А-ЯЁ][а-яё]*$")) {
                showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Отчество должно начинаться с заглавной буквы и содержать только кириллицу!");
                return;
            }

            // ЖЕСТКАЯ ВАЛИДАЦИЯ ПАСПОРТА (Так как мы ставим авто-пробел, длина должна быть ровно 11)
            if (passport.length() != 11) {
                showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Паспорт должен содержать серию и номер (10 цифр)!");
                return;
            }

            int birthYear = Integer.parseInt(birthYearStr);

            // Создаем или получаем пассажира
            Passenger passenger = passengerDao.getOrCreate(lastName, firstName, middleName, passport, birthYear);

            BigDecimal cost = new BigDecimal(costLabel.getText());
            User currentUser = Session.getCurrentUser();

            // Оформляем билет
            Ticket newTicket = ticketingService.sellTicket(
                    selectedTrip, passenger, selectedStop.getStop(), currentUser, selectedSeatNumber, cost
            );

            // Формируем чек
            String regNumber = String.format("TKT-%02d%02d-%04d",
                    java.time.LocalDate.now().getMonthValue(),
                    java.time.LocalDate.now().getDayOfMonth(),
                    (int)(Math.random() * 10000));

            String saleDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String depDate = selectedTrip.getDepartureDatetime().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            // Склеиваем ФИО для отображения в чеке
            String fullPassengerName = lastName + " " + firstName + (!middleName.isEmpty() ? " " + middleName : "");

            showReceipt(regNumber, fullPassengerName + " (Паспорт: " + passport + ")",
                    selectedTrip.getRoute().getDeparturePoint() + " - " + selectedTrip.getRoute().getDestinationPoint(),
                    selectedTrip.getBus().getModel() + " (" + selectedTrip.getBus().getLicensePlate() + ")",
                    String.valueOf(selectedSeatNumber), depDate, saleDate, cost.toString());

            // Очищаем форму для следующего клиента
            tfLastName.clear();
            tfFirstName.clear();
            tfMiddleName.clear();
            tfPassport.clear();
            birthYearField.clear();
            drawBusSeats(selectedTrip);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Год рождения должен быть числом!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
        }
    }

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