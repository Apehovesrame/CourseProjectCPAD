package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.dao.BusDaoImpl;
import ru.pin123.courseprojectcpad.dao.DriverDaoImpl;
import ru.pin123.courseprojectcpad.dao.RouteDaoImpl;
import ru.pin123.courseprojectcpad.dao.TripDaoImpl;
import ru.pin123.courseprojectcpad.model.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripEditController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(TripEditController.class);

    @FXML private ComboBox<Route> comboRoutes;
    @FXML private ComboBox<Bus> comboBuses;
    @FXML private ListView<Driver> listDrivers;
    @FXML private DatePicker dpDeparture;
    @FXML private TextField tfTime;
    @FXML private Label lblArrival;

    // Внедряем файл локализации
    @FXML private ResourceBundle resources;

    private final RouteDaoImpl routeDao = new RouteDaoImpl();
    private final BusDaoImpl busDao = new BusDaoImpl();
    private final DriverDaoImpl driverDao = new DriverDaoImpl();
    private final TripDaoImpl tripDao = new TripDaoImpl();

    private Stage dialogStage;
    private Trip trip;
    private LocalDateTime calculatedArrival = null;
    private boolean isOkClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return isOkClicked;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
        if (trip.getTripId() != null) {
            comboRoutes.getItems().stream().filter(r -> r.getRouteId() == trip.getRoute().getRouteId()).findFirst().ifPresent(comboRoutes::setValue);
            comboBuses.getItems().stream().filter(b -> b.getBusId() == trip.getBus().getBusId()).findFirst().ifPresent(comboBuses::setValue);

            dpDeparture.setValue(trip.getDepartureDatetime().toLocalDate());
            tfTime.setText(String.format("%02d:%02d", trip.getDepartureDatetime().getHour(), trip.getDepartureDatetime().getMinute()));

            updateArrivalTime();
            logger.debug("Форма заполнена данными рейса ID [{}] для редактирования.", trip.getTripId());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Сохраняем бандл для перевода ошибок
        this.resources = resources;

        try {
            comboRoutes.setItems(FXCollections.observableArrayList(routeDao.findAll()));
            comboBuses.setItems(FXCollections.observableArrayList(busDao.findAll()));
            listDrivers.setItems(FXCollections.observableArrayList(driverDao.findAll()));
            listDrivers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            dpDeparture.setValue(LocalDate.now());

            logger.info("Инициализация формы редактирования рейса. Загружено маршрутов: {}, автобусов: {}, водителей: {}.",
                    comboRoutes.getItems().size(), comboBuses.getItems().size(), listDrivers.getItems().size());

            comboRoutes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateArrivalTime());
            dpDeparture.valueProperty().addListener((obs, oldVal, newVal) -> updateArrivalTime());
            tfTime.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) updateArrivalTime();
            });

        } catch (Exception e) {
            logger.error("Критическая ошибка при инициализации формы редактирования рейса и загрузке справочных данных.", e);
            // ИСПРАВЛЕНО: Локализация ошибки
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), e.getMessage());
        }
    }

    private void updateArrivalTime() {
        Route route = comboRoutes.getValue();
        LocalDate date = dpDeparture.getValue();
        String timeStr = tfTime.getText() != null ? tfTime.getText().trim() : "";

        if (route != null && date != null && !timeStr.isEmpty()) {
            try {
                String[] timeParts = timeStr.split(":");
                int hours = Integer.parseInt(timeParts[0]);
                int mins = Integer.parseInt(timeParts[1]);

                LocalDateTime departure = date.atTime(hours, mins);
                calculatedArrival = departure.plusMinutes(route.getDurationMinutes());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                lblArrival.setText(calculatedArrival.format(formatter));
                lblArrival.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

            } catch (Exception e) {
                logger.warn("Не удалось рассчитать время прибытия. Некорректный формат времени: '{}'.", timeStr);
                // ИСПРАВЛЕНО: Локализация ошибки формата времени (ключ мы добавили на прошлом шаге)
                lblArrival.setText(resources.getString("trips.edit.error.time_format"));
                lblArrival.setStyle("-fx-text-fill: red;");
                calculatedArrival = null;
            }
        }
    }

    @FXML
    public void onSaveTripClick(ActionEvent event) {
        try {
            User currentUser = Session.getCurrentUser();
            Route selectedRoute = comboRoutes.getValue();
            Bus selectedBus = comboBuses.getValue();
            List<Driver> selectedDrivers = new ArrayList<>(listDrivers.getSelectionModel().getSelectedItems());

            if (selectedRoute == null || selectedBus == null || calculatedArrival == null) {
                // ИСПРАВЛЕНО: Локализация
                showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("trips.edit.error.empty_fields"));
                return;
            }

            if (selectedDrivers.isEmpty()) {
                // ИСПРАВЛЕНО: Локализация
                showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("trips.edit.error.no_drivers"));
                return;
            }

            String[] timeParts = tfTime.getText().trim().split(":");
            LocalDateTime departureDatetime = dpDeparture.getValue().atTime(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));

            trip.setRoute(selectedRoute);
            trip.setBus(selectedBus);
            trip.setDepartureDatetime(departureDatetime);
            trip.setArrivalDatetime(calculatedArrival);
            if (trip.getTripId() == null) trip.setCreatedByUser(currentUser);

            if (trip.getTripId() == null) {
                tripDao.create(trip, selectedDrivers);
            } else {
                tripDao.update(trip, selectedDrivers);
            }

            isOkClicked = true;
            dialogStage.close();
        } catch (Exception e) {
            logger.error("Критическая ошибка при сохранении рейса в базу данных.", e);
            // ИСПРАВЛЕНО: Локализация
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.fill_title"), e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}