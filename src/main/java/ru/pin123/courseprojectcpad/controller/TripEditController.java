package ru.pin123.courseprojectcpad.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ru.pin123.courseprojectcpad.model.Driver;
import ru.pin123.courseprojectcpad.model.Route;
import ru.pin123.courseprojectcpad.model.Trip;
import ru.pin123.courseprojectcpad.service.TripService;

import java.util.ArrayList;
import java.util.List;

public class TripEditController implements Initializable {

    @FXML private ComboBox<Route> comboRoutes;
    @FXML private ComboBox<Bus> comboBuses;
    // В JavaFX для выбора нескольких элементов часто используют ListView с MultipleSelectionModel
    @FXML private ListView<Driver> listDrivers;

    private final TripService tripService = new TripService();

    private final RouteDaoImpl routeDao = new RouteDaoImpl();
    private final BusDaoImpl busDao = new BusDaoImpl();

    // Загружаем в ListView список водителей для назначения на рейс
    List<Driver> drivers = new DriverDaoImpl().findAll();
    listDrivers.setItems(FXCollections.observableArrayList(drivers));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Загружаем данные из БД
        List<Route> routesFromDB = routeDao.findAll();
        List<Bus> busesFromDB = busDao.findAll();

        // Превращаем обычный List в специальный список JavaFX (ObservableList)
        ObservableList<Route> fxRoutes = FXCollections.observableArrayList(routesFromDB);
        ObservableList<Bus> fxBuses = FXCollections.observableArrayList(busesFromDB);

        // Устанавливаем данные в ComboBox!
        comboRoutes.setItems(fxRoutes);
        comboBuses.setItems(fxBuses);
    }

    @FXML
    public void onSaveTripClick(ActionEvent event) {
        try {
            Route selectedRoute = comboRoutes.getValue();
            if (selectedRoute == null) {
                showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите маршрут!");
                return;
            }

            // Получаем список выбранных водителей из ListView
            List<Driver> selectedDrivers = new ArrayList<>(listDrivers.getSelectionModel().getSelectedItems());

            // Собираем объект рейса
            Trip newTrip = new Trip();
            newTrip.setRoute(selectedRoute);
            // ... (установка других полей: автобус, время и т.д.)

            // Передаем в сервис на проверку и сохранение
            tripService.createTrip(newTrip, selectedDrivers);

            showAlert(Alert.AlertType.INFORMATION, "Успех", "Рейс успешно добавлен!");

        } catch (IllegalArgumentException e) {
            // Сюда прилетит ошибка про двух водителей, если маршрут дальний!
            showAlert(Alert.AlertType.ERROR, "Ошибка бизнес-логики", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Системная ошибка", "Не удалось сохранить рейс.");
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