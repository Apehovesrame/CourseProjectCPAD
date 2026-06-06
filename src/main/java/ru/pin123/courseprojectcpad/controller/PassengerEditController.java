package ru.pin123.courseprojectcpad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Passenger;

public class PassengerEditController {

    @FXML private TextField tfLastName;
    @FXML private TextField tfFirstName;
    @FXML private TextField tfMiddleName;
    @FXML private TextField tfPassport;

    private Stage dialogStage;
    private Passenger passenger;
    private boolean isOkClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
        if (passenger.getLastName() != null) tfLastName.setText(passenger.getLastName());
        if (passenger.getFirstName() != null) tfFirstName.setText(passenger.getFirstName());
        if (passenger.getMiddleName() != null) tfMiddleName.setText(passenger.getMiddleName());
        if (passenger.getPassport() != null) tfPassport.setText(passenger.getPassport());
    }

    public boolean isOkClicked() {
        return isOkClicked;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            passenger.setLastName(tfLastName.getText().trim());
            passenger.setFirstName(tfFirstName.getText().trim());
            passenger.setMiddleName(tfMiddleName.getText() != null ? tfMiddleName.getText().trim() : "");
            passenger.setPassport(tfPassport.getText().trim());

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
        if (tfPassport.getText() == null || tfPassport.getText().trim().isEmpty()) errorMessage.append("Не указаны паспортные данные!\n");

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