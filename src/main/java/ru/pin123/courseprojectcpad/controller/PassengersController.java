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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер для управления интерфейсом справочника пассажиров.
 * Обеспечивает отображение списка пассажиров в таблице, выполнение CRUD-операций
 * (добавление, редактирование, удаление), а также просмотр истории купленных билетов.
 */
public class PassengersController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(PassengersController.class);

    @FXML private TableView<Passenger> passengerTable;
    @FXML private TableColumn<Passenger, String> colLastName;
    @FXML private TableColumn<Passenger, String> colFirstName;
    @FXML private TableColumn<Passenger, String> colMiddleName;
    @FXML private TableColumn<Passenger, String> colPassport;

    // Внедряем файл локализации
    @FXML private ResourceBundle resources;

    private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();
    private final ObservableList<Passenger> passengerList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ИСПРАВЛЕНО: JavaFX внедряет bundle сюда, поэтому сохраним его
        this.resources = resources;

        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colMiddleName.setCellValueFactory(new PropertyValueFactory<>("middleName"));
        colPassport.setCellValueFactory(new PropertyValueFactory<>("passportNumber"));

        passengerTable.setItems(passengerList);
        loadData();
    }

    private void loadData() {
        try {
            passengerList.clear();
            passengerList.addAll(passengerDao.findAll());
        } catch (Exception e) {
            logger.error("Критическая ошибка при попытке загрузки списка пассажиров из БД.", e);
        }
    }

    @FXML
    private void handleAdd() {
        Passenger tempPassenger = new Passenger();
        boolean okClicked = showPassengerEditDialog(tempPassenger);
        if (okClicked) {
            passengerDao.save(tempPassenger);
            logger.info("Добавлен новый пассажир: {} {} (Паспорт: {}).",
                    tempPassenger.getLastName(), tempPassenger.getFirstName(), tempPassenger.getPassportNumber());
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
                logger.info("Изменены анкетные данные пассажира с ID [{}]: {} {}.",
                        selectedPassenger.getPassengerId(), selectedPassenger.getLastName(), selectedPassenger.getFirstName());
                loadData();
            }
        } else {
            logger.warn("Попытка редактирования: действие отменено, пассажир не выбран в таблице.");
            // ИСПРАВЛЕНО: Локализация алерта
            showAlert(resources.getString("alert.select_item"));
        }
    }

    @FXML
    private void handleDelete() {
        Passenger selectedPassenger = passengerTable.getSelectionModel().getSelectedItem();
        if (selectedPassenger != null) {
            try {
                passengerDao.delete(selectedPassenger.getPassengerId());
                logger.warn("Из базы данных удален пассажир: {} {} (ID: {}, Паспорт: {}).",
                        selectedPassenger.getLastName(), selectedPassenger.getFirstName(),
                        selectedPassenger.getPassengerId(), selectedPassenger.getPassportNumber());
                loadData();
            } catch (RuntimeException e) {
                logger.error("Не удалось удалить пассажира с ID [{}] из-за ограничений связанных ключей в БД (содержит билеты).",
                        selectedPassenger.getPassengerId(), e);
                // ИСПРАВЛЕНО: Локализация ошибки
                showAlert(resources.getString("passengers.delete_error") + ": " + e.getMessage());
            }
        } else {
            logger.warn("Попытка удаления: действие отменено, пассажир не выбран в таблице.");
            showAlert(resources.getString("alert.select_item"));
        }
    }

    private boolean showPassengerEditDialog(Passenger passenger) {
        try {
            // ИСПРАВЛЕНО: Пробрасываем resources в окно редактирования, чтобы оно тоже перевелось
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/passenger-edit-view.fxml"), resources);
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            // ИСПРАВЛЕНО: Берем заголовок из ресурсов
            dialogStage.setTitle(resources.getString("passengers.edit_title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            PassengerEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPassenger(passenger);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            logger.error("Критическая ошибка ввода-вывода при загрузке passenger-edit-view.fxml", e);
            return false;
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        // ИСПРАВЛЕНО: Заголовок алерта
        alert.setTitle(resources.getString("alert.warning.title"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleViewTicketHistory() {
        Passenger selected = passengerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            logger.warn("Попытка просмотра истории поездок прервана: пассажир не выбран.");
            showAlert(resources.getString("alert.select_item"));
            return;
        }

        logger.info("Запрошена история поездок для пассажира с ID [{}].", selected.getPassengerId());
        StringBuilder history = new StringBuilder();
        int counter = 1;

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

                    // ИСПРАВЛЕНО: Локализация строковых литералов в отчете
                    history.append(counter++).append(". ")
                            .append(resources.getString("passengers.history.trip")).append(": ").append(dep).append(" - ").append(dest).append("\n")
                            .append("   ").append(resources.getString("passengers.history.date")).append(": ").append(dateFormatted).append("\n")
                            .append("   ").append(resources.getString("passengers.history.seat")).append(": №").append(seat).append("\n")
                            .append("   ").append(resources.getString("passengers.history.cost")).append(": ").append(cost).append(" ").append(resources.getString("passengers.history.currency")).append("\n\n");
                }
            }
        } catch (Exception e) {
            logger.error("Сбой выполнения SQL при выборке архива поездок", e);
            showAlert(resources.getString("passengers.history.error") + ": " + e.getMessage());
            return;
        }

        Alert historyDialog = new Alert(Alert.AlertType.INFORMATION);
        // ИСПРАВЛЕНО: Локализация диалога истории
        historyDialog.setTitle(resources.getString("passengers.history.title"));
        historyDialog.setHeaderText(resources.getString("passengers.history.subtitle") + ": " + selected.getLastName() + " " + selected.getFirstName());

        if (history.isEmpty()) {
            historyDialog.setContentText(resources.getString("passengers.history.empty"));
        } else {
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