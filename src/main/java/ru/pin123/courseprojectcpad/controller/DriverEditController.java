package ru.pin123.courseprojectcpad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Driver;

import java.io.ByteArrayInputStream; // Для перевода байтов в картинку
import java.io.File;
import java.nio.file.Files;         // Для чтения файла в массив байт

public class DriverEditController {

    @FXML private TextField tfLastName;
    @FXML private TextField tfFirstName;
    @FXML private TextField tfMiddleName;
    @FXML private TextField tfAge;
    @FXML private TextField tfPassport;
    @FXML private ImageView imgPreview;

    private Stage dialogStage;
    private Driver driver;
    private boolean isOkClicked = false;

    // ИСПРАВЛЕНО: Теперь храним фотографию как массив байт, а не путь к файлу
    private byte[] driverImageBytes = null;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
        if (driver.getLastName() != null) tfLastName.setText(driver.getLastName());
        if (driver.getFirstName() != null) tfFirstName.setText(driver.getFirstName());
        if (driver.getMiddleName() != null) tfMiddleName.setText(driver.getMiddleName());
        if (driver.getAge() > 0) tfAge.setText(String.valueOf(driver.getAge()));
        if (driver.getPassport() != null) tfPassport.setText(driver.getPassport());

        // ИСПРАВЛЕНО: Загрузка существующего фото из байтов
        if (driver.getDriverImage() != null && driver.getDriverImage().length > 0) {
            this.driverImageBytes = driver.getDriverImage();
            ByteArrayInputStream bis = new ByteArrayInputStream(driverImageBytes);
            imgPreview.setImage(new Image(bis));
        } else {
            imgPreview.setImage(null);
        }
    }

    public boolean isOkClicked() {
        return isOkClicked;
    }

    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите фото водителя");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            try {
                // ИСПРАВЛЕНО: Читаем весь файл в массив байт
                this.driverImageBytes = Files.readAllBytes(file.toPath());
                imgPreview.setImage(new Image(file.toURI().toString()));
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(dialogStage);
                alert.setTitle("Ошибка чтения файла");
                alert.setHeaderText("Не удалось загрузить изображение");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            driver.setLastName(tfLastName.getText().trim());
            driver.setFirstName(tfFirstName.getText().trim());
            driver.setMiddleName(tfMiddleName.getText() != null ? tfMiddleName.getText().trim() : "");
            driver.setAge(Integer.parseInt(tfAge.getText().trim()));
            driver.setPassport(tfPassport.getText().trim());

            // ИСПРАВЛЕНО: Сохраняем массив байт в модель водителя
            driver.setDriverImage(driverImageBytes);

            isOkClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (tfLastName.getText() == null || tfLastName.getText().trim().isEmpty()) errorMessage.append("Не указана фамилия!\n");
        if (tfFirstName.getText() == null || tfFirstName.getText().trim().isEmpty()) errorMessage.append("Не указано имя!\n");
        if (tfPassport.getText() == null || tfPassport.getText().trim().isEmpty()) errorMessage.append("Не указан паспорт!\n");

        if (tfAge.getText() == null || tfAge.getText().trim().isEmpty()) {
            errorMessage.append("Не указан возраст!\n");
        } else {
            try {
                int age = Integer.parseInt(tfAge.getText().trim());
                if (age < 18 || age > 80) errorMessage.append("Возраст должен быть от 18 до 80 лет!\n");
            } catch (NumberFormatException e) {
                errorMessage.append("Возраст должен быть числом!\n");
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Ошибка заполнения");
            alert.setHeaderText("Пожалуйста, исправьте ошибки:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
    }
}