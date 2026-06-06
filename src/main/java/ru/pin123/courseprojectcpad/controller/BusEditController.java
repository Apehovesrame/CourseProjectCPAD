package ru.pin123.courseprojectcpad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Bus;

import java.io.File;

public class BusEditController {
    @FXML private TextField tfModel;
    @FXML private TextField tfLicensePlate;
    @FXML private TextField tfSeatCapacity;
    @FXML private ImageView imgPreview;

    private Stage dialogStage;
    private Bus bus;
    private boolean isOkClicked = false;
    private String photoPath = null;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
        if (bus.getModel() != null) tfModel.setText(bus.getModel());
        if (bus.getLicensePlate() != null) tfLicensePlate.setText(bus.getLicensePlate());
        if (bus.getSeatCapacity() > 0) tfSeatCapacity.setText(String.valueOf(bus.getSeatCapacity()));

        if (bus.getPhotoPath() != null && !bus.getPhotoPath().isEmpty()) {
            this.photoPath = bus.getPhotoPath();
            File file = new File(photoPath);
            if (file.exists()) {
                imgPreview.setImage(new Image(file.toURI().toString()));
            }
        }
    }

    public boolean isOkClicked() {
        return isOkClicked;
    }

    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите фото автобуса");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            photoPath = file.getAbsolutePath(); // Сохраняем абсолютный путь
            imgPreview.setImage(new Image(file.toURI().toString())); // Показываем превью
        }
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            bus.setModel(tfModel.getText().trim());
            bus.setLicensePlate(tfLicensePlate.getText().trim());
            bus.setSeatCapacity(Integer.parseInt(tfSeatCapacity.getText().trim()));
            bus.setPhotoPath(photoPath); // Сохраняем путь к фото в объект

            isOkClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (tfModel.getText() == null || tfModel.getText().trim().isEmpty()) errorMessage += "Не указана марка/модель автобуса!\n";
        if (tfLicensePlate.getText() == null || tfLicensePlate.getText().trim().isEmpty()) errorMessage += "Не указан госномер!\n";

        if (tfSeatCapacity.getText() == null || tfSeatCapacity.getText().trim().isEmpty()) {
            errorMessage += "Не указана вместимость!\n";
        } else {
            try {
                int capacity = Integer.parseInt(tfSeatCapacity.getText());
                if (capacity <= 0) errorMessage += "Вместимость должна быть больше нуля!\n";
            } catch (NumberFormatException e) {
                errorMessage += "Вместимость должна быть целым числом!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Ошибка заполнения");
            alert.setHeaderText("Пожалуйста, исправьте следующие ошибки:");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }
}