package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.dao.RouteDaoImpl;
import ru.pin123.courseprojectcpad.model.Route;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RoutesController implements Initializable {
    @FXML private TableView<Route> routeTable;
    @FXML private TableColumn<Route, String> colNumber;
    @FXML private TableColumn<Route, String> colFrom;
    @FXML private TableColumn<Route, String> colTo;

    // ДОБАВЛЕНО: Объявление колонки для времени в пути
    @FXML private TableColumn<Route, String> colDuration;

    private final RouteDaoImpl routeDao = new RouteDaoImpl();
    private final ObservableList<Route> routeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNumber.setCellValueFactory(new PropertyValueFactory<>("routeNumber"));
        colFrom.setCellValueFactory(new PropertyValueFactory<>("departurePoint"));
        colTo.setCellValueFactory(new PropertyValueFactory<>("destinationPoint"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));

        routeTable.setItems(routeList);
        loadData();
    }

    private void loadData() {
        routeList.clear();
        routeList.addAll(routeDao.findAll());
    }

    @FXML
    private void handleAdd() {
        Route tempRoute = new Route();
        boolean okClicked = showRouteEditDialog(tempRoute);
        if (okClicked) {
            routeDao.save(tempRoute);
            loadData();
        }
    }

    @FXML
    private void handleEdit() {
        Route selectedRoute = routeTable.getSelectionModel().getSelectedItem();
        if (selectedRoute != null) {
            boolean okClicked = showRouteEditDialog(selectedRoute);
            if (okClicked) {
                routeDao.update(selectedRoute);
                loadData();
            }
        } else {
            showAlert("Выберите маршрут в таблице для редактирования.");
        }
    }

    @FXML
    private void handleDelete() {
        Route selectedRoute = routeTable.getSelectionModel().getSelectedItem();
        if (selectedRoute != null) {
            try {
                routeDao.delete(selectedRoute.getRouteId());
                loadData();
            } catch (RuntimeException e) {
                showAlert("Невозможно удалить маршрут: " + e.getMessage());
            }
        } else {
            showAlert("Выберите маршрут в таблице для удаления.");
        }
    }

    private boolean showRouteEditDialog(Route route) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/route-edit-view.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Редактирование маршрута");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            RouteEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setRoute(route);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}