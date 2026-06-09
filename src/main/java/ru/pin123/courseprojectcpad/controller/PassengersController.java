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
import ru.pin123.courseprojectcpad.dao.PassengerDaoImpl;
import ru.pin123.courseprojectcpad.model.Passenger;
import java.util.List;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PassengersController implements Initializable {

    @FXML private TableView<Passenger> passengerTable;
    @FXML private TableColumn<Passenger, String> colLastName;
    @FXML private TableColumn<Passenger, String> colFirstName;
    @FXML private TableColumn<Passenger, String> colMiddleName;
    @FXML private TableColumn<Passenger, String> colPassport;

    private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();
    private final ObservableList<Passenger> passengerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colMiddleName.setCellValueFactory(new PropertyValueFactory<>("middleName"));
        colPassport.setCellValueFactory(new PropertyValueFactory<>("passportNumber"));

        passengerTable.setItems(passengerList);
        loadData();
    }

    private void loadData() {
        passengerList.clear();
        passengerList.addAll(passengerDao.findAll());
    }

    @FXML
    private void handleAdd() {
        Passenger tempPassenger = new Passenger();
        boolean okClicked = showPassengerEditDialog(tempPassenger);
        if (okClicked) {
            passengerDao.save(tempPassenger);
            loadData();
        }
    }

    @FXML
    private void handleEdit() {
        Passenger selectedPassenger = passengerTable.getSelectionModel().getSelectedItem();
        if (selectedPassenger != null) {
            boolean okClicked = showPassengerEditDialog(selectedPassenger);
            if (okClicked) {
                passengerDao.update(selectedPassenger);
                loadData();
            }
        } else {
            showAlert("Выберите пассажира в таблице для редактирования.");
        }
    }

    @FXML
    private void handleDelete() {
        Passenger selectedPassenger = passengerTable.getSelectionModel().getSelectedItem();
        if (selectedPassenger != null) {
            try {
                passengerDao.delete(selectedPassenger.getPassengerId());
                loadData();
            } catch (RuntimeException e) {
                showAlert("Невозможно удалить пассажира: " + e.getMessage());
            }
        } else {
            showAlert("Выберите пассажира в таблице для удаления.");
        }
    }

    private boolean showPassengerEditDialog(Passenger passenger) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/passenger-edit-view.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Данные пассажира");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            PassengerEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPassenger(passenger);

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
    @FXML
    private void handleViewTicketHistory() {
        ru.pin123.courseprojectcpad.model.Passenger selected = passengerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Выберите пассажира из таблицы!").showAndWait();
            return;
        }

        ru.pin123.courseprojectcpad.dao.TicketDaoImpl ticketDao = new ru.pin123.courseprojectcpad.dao.TicketDaoImpl();
        // Загружаем все билеты из системы
        List<ru.pin123.courseprojectcpad.model.Ticket> allTickets = ticketDao.findAll();

        StringBuilder history = new StringBuilder();
        int counter = 1;

        for (ru.pin123.courseprojectcpad.model.Ticket ticket : allTickets) {
            // Маппим билеты, принадлежащие конкретно этому пассажиру
            if (ticket.getPassenger() != null && ticket.getPassenger().getPassengerId().equals(selected.getPassengerId())) {
                history.append(counter++).append(". ")
                        .append("Рейс: ").append(ticket.getTrip().getRoute().toString()).append("\n")
                        .append("   Дата отправления: ").append(ticket.getTrip().getDepartureDatetime().toString()).append("\n")
                        .append("   Место в салоне: №").append(ticket.getSeatNumber()).append("\n")
                        .append("   Стоимость: ").append(ticket.getCost()).append(" руб.\n\n");
            }
        }

        Alert historyDialog = new Alert(Alert.AlertType.INFORMATION);
        historyDialog.setTitle("Архив поездок пассажира");
        historyDialog.setHeaderText("История билетов для: " + selected.getLastName() + " " + selected.getFirstName());

        if (history.length() == 0) {
            historyDialog.setContentText("У данного пассажира архив поездок пуст (билеты еще не приобретались).");
        } else {
            historyDialog.setContentText(history.toString());
        }
        historyDialog.showAndWait();
    }
}