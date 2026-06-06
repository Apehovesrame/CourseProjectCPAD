package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ru.pin123.courseprojectcpad.dao.BusDaoImpl;
import ru.pin123.courseprojectcpad.dao.DriverDaoImpl;
import ru.pin123.courseprojectcpad.dao.RouteDaoImpl;
import ru.pin123.courseprojectcpad.model.*;
import ru.pin123.courseprojectcpad.service.TripService;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TripEditController implements Initializable {

    @FXML private ComboBox<Route> comboRoutes;
    @FXML private ComboBox<Bus> comboBuses;
    @FXML private ListView<Driver> listDrivers;

    private final TripService tripService = new TripService();
    private final RouteDaoImpl routeDao = new RouteDaoImpl();
    private final BusDaoImpl busDao = new BusDaoImpl();
    private final DriverDaoImpl driverDao = new DriverDaoImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            List<Route> routesFromDB = routeDao.findAll();
            List<Bus> busesFromDB = busDao.findAll();
            List<Driver> driversFromDB = driverDao.findAll();

            comboRoutes.setItems(FXCollections.observableArrayList(routesFromDB));
            comboBuses.setItems(FXCollections.observableArrayList(busesFromDB));
            listDrivers.setItems(FXCollections.observableArrayList(driversFromDB));

            listDrivers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка БД", "Не удалось загрузить данные: " + e.getMessage());
        }
    }

    @FXML
    public void onSaveTripClick(ActionEvent event) {
        try {
            // Получаем пользователя, создающего рейс, из глобальной сессии
            User currentUser = Session.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Ошибка сессии", "Пользователь не авторизован в системе!");
                return;
            }

            Route selectedRoute = comboRoutes.getValue();
            Bus selectedBus = comboBuses.getValue();

            if (selectedRoute == null || selectedBus == null) {
                showAlert(Alert.AlertType.WARNING, "Внимание", "Пожалуйста, выберите маршрут и автобус!");
                return;
            }

            List<Driver> selectedDrivers = new ArrayList<>(listDrivers.getSelectionModel().getSelectedItems());

            Trip newTrip = new Trip();
            newTrip.setRoute(selectedRoute);
            newTrip.setBus(selectedBus);
            newTrip.setCreatedByUser(currentUser); // Передаем реального авторизованного юзера

            // Заглушка для демонстрации дат (в продакшене заменяется на данные из DatePicker)
            newTrip.setDepartureDatetime(java.time.LocalDateTime.now().toString());
            newTrip.setArrivalDatetime(java.time.LocalDateTime.now().plusHours(5).toString());

            tripService.createTrip(newTrip, selectedDrivers);

            showAlert(Alert.AlertType.INFORMATION, "Успех", "Рейс успешно добавлен!");

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка бизнес-логики", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Системная ошибка", "Не удалось сохранить рейс: " + e.getMessage());
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