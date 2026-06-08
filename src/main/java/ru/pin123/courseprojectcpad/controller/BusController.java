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
import java.util.Locale;
import java.util.ResourceBundle;

public class BusController {
    @FXML private TableView<Bus> busTable;
    @FXML private TableColumn<Bus, Long> idColumn;
    @FXML private TableColumn<Bus, String> modelColumn;
    @FXML private TableColumn<Bus, String> plateColumn;
    @FXML private TableColumn<Bus, Number> capacityColumn;

    // Добавили ImageView для отображения фото выбранного автобуса
    @FXML private ImageView busImage;

    private final ObservableList<Bus> busList = FXCollections.observableArrayList();
    private final BusService busService = new BusService(); // Подключаем сервис

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getBusId()));
        modelColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getModel()));
        plateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLicensePlate()));
        capacityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSeatCapacity()));

        // Добавляем слушатель: при клике на строку таблицы показываем фото автобуса
        busTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showBusDetails(newValue)
        );

        busTable.setItems(busList);
        loadData(); // Загружаем данные из БД
    }

    // КЛЮЧЕВОЙ МОМЕНТ: Изменили логику отображения картинки из byte[]
    private void showBusDetails(Bus bus) {
        if (bus != null && bus.getBusImage() != null && bus.getBusImage().length > 0) {
            // Оборачиваем массив байт во входной поток и передаем в JavaFX Image
            ByteArrayInputStream bis = new ByteArrayInputStream(bus.getBusImage());
            busImage.setImage(new Image(bis));
        } else {
            busImage.setImage(null); // Очищаем картинку, если фото в базе нет
        }
    }

    private void loadData() {
        busList.clear();
        try {
            busList.addAll(busService.getAllBuses());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onAddBus() {
        Bus newBus = new Bus();
        boolean okClicked = showBusEditDialog(newBus);
        if (okClicked) {
            busService.saveBus(newBus);
            loadData(); // Обновляем таблицу
        }
    }

    @FXML
    private void onEditBus() {
        Bus selectedBus = busTable.getSelectionModel().getSelectedItem();
        if (selectedBus != null) {
            boolean okClicked = showBusEditDialog(selectedBus);
            if (okClicked) {
                // В вашем сервисе метод saveBus, судя по всему, работает и как update
                busService.saveBus(selectedBus);
                loadData();
                showBusDetails(selectedBus); // Обновляем картинку
            }
        } else {
            new Alert(Alert.AlertType.WARNING, "Выберите автобус!").showAndWait();
        }
    }

    @FXML
    private void onDeleteBus() {
        Bus selectedBus = busTable.getSelectionModel().getSelectedItem();
        if (selectedBus != null) {
            busService.deleteBus(selectedBus.getBusId());
            loadData();
        } else {
            new Alert(Alert.AlertType.WARNING, "Выберите автобус для удаления!").showAndWait();
        }
    }

    // Открытие модального окна (Задание из Лабораторной №7)
    private boolean showBusEditDialog(Bus bus) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
            // ИСПРАВЛЕН ПУТЬ НА bus-edit-view.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/bus-edit-view.fxml"), bundle);
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Автобус");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            // Передаем объект автобуса в контроллер диалога
            BusEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setBus(bus);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}