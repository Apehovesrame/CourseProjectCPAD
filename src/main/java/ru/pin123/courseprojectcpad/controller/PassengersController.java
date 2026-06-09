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
 * * @author Зонин М.Д.
 * @version 1.0
 */
public class PassengersController implements Initializable {

    /**
     * Логгер SLF4J для логирования бизнес-событий и критических исключений в классе.
     */
    private static final Logger logger = LoggerFactory.getLogger(PassengersController.class);

    @FXML private TableView<Passenger> passengerTable;
    @FXML private TableColumn<Passenger, String> colLastName;
    @FXML private TableColumn<Passenger, String> colFirstName;
    @FXML private TableColumn<Passenger, String> colMiddleName;
    @FXML private TableColumn<Passenger, String> colPassport;

    /**
     * Объект доступа к данным (DAO) для управления сущностями пассажиров в БД.
     */
    private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();

    /**
     * Наблюдаемый список для автоматической синхронизации данных UI с моделью.
     */
    private final ObservableList<Passenger> passengerList = FXCollections.observableArrayList();

    /**
     * Инициализирует контроллер при загрузке fxml-формы. Настраивает привязку
     * колонок таблицы к свойствам модели Passenger и загружает начальные данные.
     * * @param location URL-адрес, использованный для разрешения относительных путей корневого объекта.
     * @param resources Ресурсы, используемые для локализации корневого объекта.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colMiddleName.setCellValueFactory(new PropertyValueFactory<>("middleName"));
        colPassport.setCellValueFactory(new PropertyValueFactory<>("passportNumber"));

        passengerTable.setItems(passengerList);
        loadData();
    }

    /**
     * Синхронизирует данные из таблицы PostgreSQL с наблюдаемым списком в интерфейсе.
     */
    private void loadData() {
        try {
            passengerList.clear();
            passengerList.addAll(passengerDao.findAll());
        } catch (Exception e) {
            logger.error("Критическая ошибка при попытке загрузки списка пассажиров из БД.", e);
        }
    }

    /**
     * Обработчик нажатия кнопки добавления. Открывает диалоговое окно создания
     * пассажира и, в случае успеха, сохраняет запись в базу данных.
     */
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

    /**
     * Обработчик нажатия кнопки изменения. Извлекает выбранного из таблицы
     * пассажира, открывает окно редактирования и обновляет данные в БД.
     */
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
            showAlert("Выберите пассажира в таблице для редактирования.");
        }
    }

    /**
     * Обработчик нажатия кнопки удаления. Удаляет выбранного в таблице
     * пассажира из базы данных. Блокирует операцию при нарушении целостности данных.
     */
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
                showAlert("Невозможно удалить пассажира: " + e.getMessage());
            }
        } else {
            logger.warn("Попытка удаления: действие отменено, пассажир не выбран в таблице.");
            showAlert("Выберите пассажира в таблице для удаления.");
        }
    }

    /**
     * Создает и отображает модальное диалоговое окно для добавления или редактирования
     * параметров пассажира.
     * * @param passenger Ссылка на изменяемый или создаваемый объект пассажира.
     * @return true, если пользователь нажал кнопку сохранения, в противном случае false.
     */
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
            logger.error("Критическая ошибка ввода-вывода при загрузке passenger-edit-view.fxml", e);
            return false;
        }
    }

    /**
     * Отображает системное всплывающее окно типа WARNING для уведомления пользователя.
     * * @param content Текстовое сообщение, выводимое внутри диалогового окна.
     */
    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Извлекает из базы данных архив билетов, купленных выбранным пассажиром,
     * и форматирует их в виде текстового отчета во всплывающем окне.
     */
    @FXML
    private void handleViewTicketHistory() {
        Passenger selected = passengerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            logger.warn("Попытка просмотра истории поездок прервана: пассажир не выбран.");
            showAlert("Выберите пассажира из таблицы!");
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

                    history.append(counter++).append(". ")
                            .append("Рейс: ").append(dep).append(" - ").append(dest).append("\n")
                            .append("   Дата: ").append(dateFormatted).append("\n")
                            .append("   Место: №").append(seat).append("\n")
                            .append("   Стоимость: ").append(cost).append(" руб.\n\n");
                }
            }
        } catch (Exception e) {
            logger.error("Сбой выполнения SQL при выборке архива поездок для пассажира ID [{}]", selected.getPassengerId(), e);
            showAlert("Ошибка при загрузке истории: " + e.getMessage());
            return;
        }

        Alert historyDialog = new Alert(Alert.AlertType.INFORMATION);
        historyDialog.setTitle("Архив поездок");
        historyDialog.setHeaderText("История билетов: " + selected.getLastName() + " " + selected.getFirstName());

        if (history.isEmpty()) {
            historyDialog.setContentText("Билеты еще не приобретались.");
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