package ru.pin123.courseprojectcpad.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pin123.courseprojectcpad.dao.AuditDaoImpl;
import ru.pin123.courseprojectcpad.model.AuditLog;

import java.net.URL;
import java.util.ResourceBundle;

public class AuditController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);
    private final AuditDaoImpl auditDao = new AuditDaoImpl();

    @FXML private TableView<AuditLog> auditTable;
    @FXML private TableColumn<AuditLog, String> colId;
    @FXML private TableColumn<AuditLog, String> colDate;
    @FXML private TableColumn<AuditLog, String> colAction;
    @FXML private TableColumn<AuditLog, String> colTable;
    @FXML private TableColumn<AuditLog, String> colUser;
    @FXML private TableColumn<AuditLog, String> colOldData;
    @FXML private TableColumn<AuditLog, String> colNewData;

    @FXML private ResourceBundle resources;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        setupTableColumns();
        loadAuditData();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLogId().toString()));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDate()));
        colAction.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getActionType()));
        colTable.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTableName()));
        colUser.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDbUser()));
        colOldData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOldData()));
        colNewData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNewData()));
    }

    @FXML
    private void handleRefresh() {
        loadAuditData();
    }

    private void loadAuditData() {
        try {
            ObservableList<AuditLog> logs = FXCollections.observableArrayList(auditDao.findAll());
            auditTable.setItems(logs);
            logger.info("Журнал аудита успешно загружен. Записей: {}", logs.size());
        } catch (Exception e) {
            logger.error("Ошибка загрузки журнала аудита", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resources.getString("alert.error.title"));
            alert.setHeaderText(null);
            alert.setContentText("Не удалось загрузить журнал аудита: " + e.getMessage());
            alert.showAndWait();
        }
    }
}