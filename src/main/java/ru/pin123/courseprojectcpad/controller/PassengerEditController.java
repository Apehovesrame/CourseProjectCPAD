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

    // ДОБАВЛЕНО ПОЛЕ: Год рождения
    @FXML private TextField tfBirthYear;

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

        // ИСПРАВЛЕНО: getPassportNumber()
        if (passenger.getPassportNumber() != null) tfPassport.setText(passenger.getPassportNumber());

        if (passenger.getBirthYear() > 0) tfBirthYear.setText(String.valueOf(passenger.getBirthYear()));
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

            // ИСПРАВЛЕНО: setPassportNumber()
            passenger.setPassportNumber(tfPassport.getText().trim());

            passenger.setBirthYear(Integer.parseInt(tfBirthYear.getText().trim()));

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

        if (tfBirthYear.getText() == null || tfBirthYear.getText().trim().isEmpty()) {
            errorMessage.append("Не указан год рождения!\n");
        } else {
            try {
                int year = Integer.parseInt(tfBirthYear.getText().trim());
                if (year < 1900 || year > java.time.LocalDate.now().getYear()) {
                    errorMessage.append("Некорректный год рождения!\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Год рождения должен быть числом!\n");
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