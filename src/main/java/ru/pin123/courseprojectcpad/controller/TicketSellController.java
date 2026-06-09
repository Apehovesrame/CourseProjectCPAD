package ru.pin123.courseprojectcpad.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pin123.courseprojectcpad.dao.PassengerDaoImpl;
import ru.pin123.courseprojectcpad.dao.StopDaoImpl;
import ru.pin123.courseprojectcpad.dao.TripDaoImpl;
import ru.pin123.courseprojectcpad.model.*;
import ru.pin123.courseprojectcpad.service.TicketingService;

import java.io.IOException;
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
    @FXML private TextField tfLastName;
    @FXML private TextField tfFirstName;
    @FXML private TextField tfMiddleName;
    @FXML private TextField tfPassport;

    // Внедряем ресурсы локализации
    @FXML private ResourceBundle resources;

    private Integer selectedSeatNumber = null;
    private Button selectedSeatButton = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Сохраняем внедренный бандл
        this.resources = resources;
        logger.info("Инициализация экрана продажи билетов.");

        try {
            List<Trip> trips = tripDao.findAll();
            tripsListView.setItems(FXCollections.observableArrayList(trips));
        } catch (Exception e) {
            logger.error("Критическая ошибка при загрузке списка рейсов из БД.", e);
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("sell.error.load_trips"));
        }

        tripsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) onTripSelected(newVal);
        });

        stopComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                costLabel.setText(newVal.getPrice().toString());
            }
        });

        setupPassportMask(tfPassport);
    }

    private void setupPassportMask(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            String digits = newValue.replaceAll("[^\\d]", "");
            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }
            StringBuilder formatted = new StringBuilder(digits);
            if (formatted.length() > 4) {
                formatted.insert(4, " ");
            }
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
                // Передаем ресурсы внутрь StopItem для перевода валюты
                stopItems.add(new StopItem(allStops.get(i), price, resources.getString("currency")));
            }
            stopComboBox.setItems(FXCollections.observableArrayList(stopItems));

            drawBusSeats(trip);

        } catch (Exception e) {
            logger.error("Ошибка при выборе рейса ID [{}]", trip.getTripId(), e);
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("sell.error.load_details") + ":\n" + e.getMessage());
        }
    }

    private void drawBusSeats(Trip trip) {
        seatsGrid.getChildren().clear();
        seatsGrid.getColumnConstraints().clear();
        selectedSeatNumber = null;
        selectedSeatButton = null;

        if (trip.getBus() == null) {
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("sell.error.no_bus"));
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
            clearHighlights();
            boolean hasError = false;

            Trip selectedTrip = tripsListView.getSelectionModel().getSelectedItem();
            StopItem selectedStop = stopComboBox.getValue();

            String lastName = tfLastName.getText() != null ? tfLastName.getText().trim() : "";
            String firstName = tfFirstName.getText() != null ? tfFirstName.getText().trim() : "";
            String middleName = tfMiddleName.getText() != null ? tfMiddleName.getText().trim() : "";
            String passport = tfPassport.getText() != null ? tfPassport.getText().trim() : "";

            if (lastName.isEmpty()) { highlightField(tfLastName); hasError = true; }
            if (firstName.isEmpty()) { highlightField(tfFirstName); hasError = true; }
            if (passport.isEmpty()) { highlightField(tfPassport); hasError = true; }
            if (selectedStop == null) { highlightField(stopComboBox); hasError = true; }

            if (selectedTrip == null || selectedSeatNumber == null) hasError = true;

            if (hasError) {
                showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("sell.validation.empty"));
                return;
            }

            if (!lastName.matches("^[А-ЯЁ][а-яё]*(-[А-ЯЁ][а-яё]*)?$") || !firstName.matches("^[А-ЯЁ][а-яё]*$")) {
                showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.fill_title"), resources.getString("sell.validation.fio"));
                highlightField(tfLastName);
                highlightField(tfFirstName);
                return;
            }
            if (!middleName.isEmpty() && !middleName.matches("^[А-ЯЁ][а-яё]*$")) {
                showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.fill_title"), resources.getString("sell.validation.middlename"));
                highlightField(tfMiddleName);
                return;
            }

            if (passport.length() != 11) {
                showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.fill_title"), resources.getString("sell.validation.passport"));
                highlightField(tfPassport);
                return;
            }

            Passenger passenger = passengerDao.getOrCreate(lastName, firstName, middleName, passport, 0);

            BigDecimal cost = new BigDecimal(costLabel.getText());
            User currentUser = Session.getCurrentUser();

            Ticket newTicket = ticketingService.sellTicket(
                    selectedTrip, passenger, selectedStop.getStop(), currentUser, selectedSeatNumber, cost
            );

            String regNumber = String.format("TKT-%02d%02d-%04d",
                    java.time.LocalDate.now().getMonthValue(),
                    java.time.LocalDate.now().getDayOfMonth(),
                    (int)(Math.random() * 10000));

            String saleDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String depDate = selectedTrip.getDepartureDatetime().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String fullPassengerName = lastName + " " + firstName + (!middleName.isEmpty() ? " " + middleName : "");

            // Локализованный заголовок паспорта внутри чека
            String passPrefix = resources.getString("col.passport");
            showReceipt(regNumber, fullPassengerName + " (" + passPrefix + ": " + passport + ")",
                    selectedTrip.getRoute().getDeparturePoint() + " - " + selectedTrip.getRoute().getDestinationPoint(),
                    selectedTrip.getBus().getModel() + " (" + selectedTrip.getBus().getLicensePlate() + ")",
                    String.valueOf(selectedSeatNumber), depDate, saleDate, cost.toString());

            tfLastName.clear();
            tfFirstName.clear();
            tfMiddleName.clear();
            tfPassport.clear();
            clearHighlights();
            drawBusSeats(selectedTrip);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), e.getMessage());
        }
    }

    private void showReceipt(String regNum, String pass, String route, String bus, String seat, String dep, String sale, String cost) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/ticket-receipt-view.fxml"), resources);
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("receipt.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            TicketReceiptController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTicketData(regNum, pass, route, bus, seat, dep, sale, cost);

            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.error("Ошибка при загрузке чека", e);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearHighlights() {
        String defaultStyle = "-fx-border-color: transparent;";
        tfLastName.setStyle(defaultStyle);
        tfFirstName.setStyle(defaultStyle);
        tfPassport.setStyle(defaultStyle);
        stopComboBox.setStyle(defaultStyle);
    }

    private void highlightField(Control field) {
        field.setStyle("-fx-border-color: red; -fx-border-radius: 4; -fx-border-width: 2;");
    }

    public static class StopItem {
        private final Stop stop;
        private final BigDecimal price;
        private final String currencyStr;

        public StopItem(Stop stop, BigDecimal price, String currencyStr) {
            this.stop = stop;
            this.price = price;
            this.currencyStr = currencyStr;
        }

        public Stop getStop() { return stop; }
        public BigDecimal getPrice() { return price; }

        @Override
        public String toString() {
            // ИСПРАВЛЕНО: Теперь валюта подтягивается из ресурсов
            return stop.getName() + " — " + price + " " + currencyStr;
        }
    }
}