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
        Passenger selected = passengerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите пассажира из таблицы!");
            return;
        }

        StringBuilder history = new StringBuilder();
        int counter = 1;

        // Прямой 100% рабочий запрос в БД для объединения рейсов, маршрутов и билетов
        String sql = "SELECT r.departure_point, r.destination_point, tr.departure_datetime, t.seat_number, t.cost " +
                "FROM tickets t " +
                "JOIN trips tr ON t.trip_id = tr.trip_id " +
                "JOIN routes r ON tr.route_id = r.route_id " +
                "WHERE t.passenger_id = ? " +
                "ORDER BY tr.departure_datetime DESC";

        try (java.sql.Connection conn = ru.pin123.courseprojectcpad.DBHelper.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, selected.getPassengerId());
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dep = rs.getString("departure_point");
                    String dest = rs.getString("destination_point");
                    java.sql.Timestamp datetime = rs.getTimestamp("departure_datetime");
                    int seat = rs.getInt("seat_number");
                    java.math.BigDecimal cost = rs.getBigDecimal("cost");

                    String dateFormatted = datetime != null ?
                            datetime.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "Н/Д";

                    history.append(counter++).append(". ")
                            .append("Рейс: ").append(dep).append(" - ").append(dest).append("\n")
                            .append("   Дата: ").append(dateFormatted).append("\n")
                            .append("   Место: №").append(seat).append("\n")
                            .append("   Стоимость: ").append(cost).append(" руб.\n\n");
                }
            }
        } catch (Exception e) {
            showAlert("Ошибка при загрузке истории: " + e.getMessage());
            return;
        }

        Alert historyDialog = new Alert(Alert.AlertType.INFORMATION);
        historyDialog.setTitle("Архив поездок");
        historyDialog.setHeaderText("История билетов: " + selected.getLastName() + " " + selected.getFirstName());

        if (history.isEmpty()) {
            historyDialog.setContentText("Билеты еще не приобретались.");
        } else {
            // Добавляем окно прокрутки для удобства
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(history.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            historyDialog.getDialogPane().setContent(textArea);
        }
        historyDialog.showAndWait();
    }
}