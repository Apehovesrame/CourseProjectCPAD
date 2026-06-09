package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Bus;
import ru.pin123.courseprojectcpad.service.BusService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер для управления интерфейсом справочника автопарка (автобусов).
 * Обеспечивает вывод транспортных средств в табличное представление, динамическое
 * отображение медиа-контента (фотографий) из BLOB/Bytea хранилища СУБД,
 * а также выполнение CRUD-операций через слой бизнес-логики.
 */
public class BusController {

    private static final Logger logger = LoggerFactory.getLogger(BusController.class);

    @FXML private TableView<Bus> busTable;
    @FXML private TableColumn<Bus, Long> idColumn;
    @FXML private TableColumn<Bus, String> modelColumn;
    @FXML private TableColumn<Bus, String> plateColumn;
    @FXML private TableColumn<Bus, Number> capacityColumn;

    @FXML private ImageView busImage;

    @FXML private ResourceBundle resources;

    private final ObservableList<Bus> busList = FXCollections.observableArrayList();
    private final BusService busService = new BusService();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBusId()));
        modelColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getModel()));
        plateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLicensePlate()));
        capacityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSeatCapacity()));

        busTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showBusDetails(newValue)
        );

        busTable.setItems(busList);
        loadData();
    }

    private void showBusDetails(Bus bus) {
        if (bus != null && bus.getBusImage() != null && bus.getBusImage().length > 0) {
            logger.debug("Загрузка бинарного контента изображения для автобуса ID [{}]. Size: {} bytes", bus.getBusId(), bus.getBusImage().length);
            ByteArrayInputStream bis = new ByteArrayInputStream(bus.getBusImage());
            busImage.setImage(new Image(bis));
        } else {
            busImage.setImage(null);
        }
    }

    private void loadData() {
        busList.clear();
        try {
            busList.addAll(busService.getAllBuses());
        } catch (Exception e) {
            logger.error("Не удалось выполнить чтение данных из репозитория автопарка.", e);
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("buses.load_error") + ": " + e.getMessage());
        }
    }

    @FXML
    private void onAddBus() {
        Bus newBus = new Bus();
        boolean okClicked = showBusEditDialog(newBus);
        if (okClicked) {
            busService.saveBus(newBus);
            logger.info("В реестр автопарка успешно добавлен новый автобус: {} (Гос. номер: {}).",
                    newBus.getModel(), newBus.getLicensePlate());
            loadData();
        }
    }

    @FXML
    private void onEditBus() {
        Bus selectedBus = busTable.getSelectionModel().getSelectedItem();
        if (selectedBus != null) {
            boolean okClicked = showBusEditDialog(selectedBus);
            if (okClicked) {
                busService.saveBus(selectedBus);
                logger.info("Обновлена конфигурация транспортного средства с ID [{}]: {} ({}).",
                        selectedBus.getBusId(), selectedBus.getModel(), selectedBus.getLicensePlate());
                loadData();
                showBusDetails(selectedBus);
            }
        } else {
            logger.warn("Действие отменено: пользователь попытался вызвать редактирование без выбора автобуса.");
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("alert.select_item"));
        }
    }

    @FXML
    private void onDeleteBus() {
        Bus selectedBus = busTable.getSelectionModel().getSelectedItem();
        if (selectedBus != null) {
            try {
                busService.deleteBus(selectedBus.getBusId());
                logger.warn("Из реестра автопарка безвозвратно удален автобус с ID [{}], Гос. номер: {}.",
                        selectedBus.getBusId(), selectedBus.getLicensePlate());
                loadData();
                showBusDetails(null);
            } catch (Exception e) {
                logger.error("Ошибка каскадного удаления: автобус ID [{}] задействован в активных рейсах расписания.", selectedBus.getBusId(), e);
                showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("buses.delete_error"));
            }
        } else {
            logger.warn("Действие отменено: пользователь попытался удалить автобус без выбора строки.");
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("alert.select_item"));
        }
    }

    private boolean showBusEditDialog(Bus bus) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/bus-edit-view.fxml"), resources);
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(resources.getString("buses.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            BusEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setBus(bus);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            logger.error("Критическая ошибка I/O при попытке десериализации fxml-представления bus-edit-view.fxml", e);
            return false;
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