package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.dao.DriverDaoImpl;
import ru.pin123.courseprojectcpad.model.Driver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriversController implements Initializable {

    // ИСПРАВЛЕНО: Инициализировали статическую переменную логгера для текущего класса
    private static final Logger logger = LoggerFactory.getLogger(DriversController.class);

    @FXML private TableView<Driver> driverTable;
    @FXML private TableColumn<Driver, String> colLastName;
    @FXML private TableColumn<Driver, String> colFirstName;
    @FXML private TableColumn<Driver, String> colMiddleName;

    // ВЕРНУЛИ КОЛОНКИ
    @FXML private TableColumn<Driver, Integer> colAge;
    @FXML private TableColumn<Driver, String> colPassport;

    // ЭЛЕМЕНТЫ КАРТОЧКИ
    @FXML private ImageView imgDriverPhoto;
    @FXML private Label lblNoPhoto;
    @FXML private Label lblDriverName;

    private final DriverDaoImpl driverDao = new DriverDaoImpl();
    private final ObservableList<Driver> driverList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colMiddleName.setCellValueFactory(new PropertyValueFactory<>("middleName"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colPassport.setCellValueFactory(new PropertyValueFactory<>("passport"));

        driverTable.setItems(driverList);
        loadData();

        driverTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            showDriverDetails(newSelection);
        });
    }

    private void loadData() {
        try {
            driverList.clear();
            driverList.addAll(driverDao.findAll());
        } catch (Exception e) {
            logger.error("Критическая ошибка при попытке загрузки списка водителей из слоя DAO", e);
        }
    }

    // МЕТОД ОБНОВЛЕНИЯ КРАСИВОЙ КАРТОЧКИ
    private void showDriverDetails(Driver driver) {
        if (driver != null) {
            lblDriverName.setText(driver.getLastName() + " " + driver.getFirstName());

            if (driver.getDriverImage() != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(driver.getDriverImage());
                imgDriverPhoto.setImage(new Image(bis));
                lblNoPhoto.setVisible(false); // Прячем надпись "Нет фото"
            } else {
                imgDriverPhoto.setImage(null);
                lblNoPhoto.setVisible(true); // Показываем заглушку
            }
        } else {
            lblDriverName.setText("Выберите водителя");
            imgDriverPhoto.setImage(null);
            lblNoPhoto.setVisible(true);
        }
    }

    @FXML
    private void handleAdd() {
        Driver tempDriver = new Driver();
        boolean okClicked = showDriverEditDialog(tempDriver);
        if (okClicked) {
            driverDao.save(tempDriver);
            // ДОБАВЛЕНО ЛОГИРОВАНИЕ УСПЕШНОГО ДОБАВЛЕНИЯ (INFO)
            logger.info("Добавлен новый водитель: {} {} {} (Паспорт: {}).",
                    tempDriver.getLastName(), tempDriver.getFirstName(), tempDriver.getMiddleName(), tempDriver.getPassport());
            loadData();
        }
    }

    @FXML
    private void handleEdit() {
        Driver selectedDriver = driverTable.getSelectionModel().getSelectedItem();
        if (selectedDriver != null) {
            boolean okClicked = showDriverEditDialog(selectedDriver);
            if (okClicked) {
                driverDao.update(selectedDriver);
                // ДОБАВЛЕНО ЛОГИРОВАНИЕ ИЗМЕНЕНИЯ ДАННЫХ (INFO)
                logger.info("Изменены анкетные данные водителя с ID [{}]: {} {}.",
                        selectedDriver.getDriverId(), selectedDriver.getLastName(), selectedDriver.getFirstName());
                loadData();
                showDriverDetails(selectedDriver);
            }
        } else {
            logger.warn("Попытка редактирования: действие отменено, водитель не выбран в таблице.");
            showAlert("Выберите водителя в таблице для редактирования.");
        }
    }

    @FXML
    private void handleDelete() {
        Driver selectedDriver = driverTable.getSelectionModel().getSelectedItem();
        if (selectedDriver != null) {
            try {
                driverDao.delete(selectedDriver.getDriverId());
                // ДОБАВЛЕНО ОПАСНОЕ ЛОГИРОВАНИЕ УДАЛЕНИЯ (WARN)
                logger.warn("Из базы данных удален водитель: {} {} (ID: {}, Паспорт: {}).",
                        selectedDriver.getLastName(), selectedDriver.getFirstName(), selectedDriver.getDriverId(), selectedDriver.getPassport());
                loadData();
                showDriverDetails(null); // Очищаем карточку
            } catch (RuntimeException e) {
                // ДОБАВЛЕНО ЛОГИРОВАНИЕ ОШИБКИ УДАЛЕНИЯ СВЯЗАННОЙ СУЩНОСТИ (ERROR)
                logger.error("Не удалось удалить водителя с ID [{}] из-за ограничений внешнего ключа в БД.", selectedDriver.getDriverId(), e);
                showAlert("Невозможно удалить водителя: " + e.getMessage());
            }
        } else {
            logger.warn("Попытка удаления: действие отменено, водитель не выбран в таблице.");
            showAlert("Выберите водителя в таблице для удаления.");
        }
    }

    private boolean showDriverEditDialog(Driver driver) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/driver-edit-view.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Данные водителя");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(page));

            DriverEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setDriver(driver);

            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            // ИСПРАВЛЕНО: Заменили немой printStackTrace() на полноценную фиксацию ошибки в логах
            logger.error("Критическая ошибка ввода-вывода интерфейса при загрузке fxml-формы driver-edit-view.fxml", e);
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