package ru.pin123.courseprojectcpad.controller;

import javafx.application.Platform;
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
    @FXML private TextField tfBirthYear;

    private Stage dialogStage;
    private Passenger passenger;
    private boolean isOkClicked = false;

    // ВЫЗЫВАЕТСЯ АВТОМАТИЧЕСКИ ПРИ ОТКРЫТИИ ОКНА
    @FXML
    public void initialize() {
        // Подключаем автоматическую маску паспорта сразу при загрузке
        setupPassportField(tfPassport);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
        if (passenger.getLastName() != null) tfLastName.setText(passenger.getLastName());
        if (passenger.getFirstName() != null) tfFirstName.setText(passenger.getFirstName());
        if (passenger.getMiddleName() != null) tfMiddleName.setText(passenger.getMiddleName());
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

    // ЛОГИКА МАСКИ ДЛЯ ПАСПОРТА
    private void setupPassportField(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;

            String digits = newValue.replaceAll("[^\\d]", "");

            if (digits.length() > 10) {
                digits = digits.substring(0, 10);
            }

            StringBuilder formatted = new StringBuilder(digits);
            if (formatted.length() > 4) {
                formatted.insert(4, " ");
            }

            if (!newValue.equals(formatted.toString())) {
                textField.setText(formatted.toString());
                // Сдвигаем курсор в конец, чтобы было удобно печатать
                Platform.runLater(textField::end);
            }
        });
    }

    // ПРОВЕРКА ДАННЫХ ПЕРЕД СОХРАНЕНИЕМ
    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        // Проверка Фамилии
        if (tfLastName.getText() == null || tfLastName.getText().trim().isEmpty()) {
            errorMessage.append("Не указана фамилия!\n");
        } else if (!isFioValid(tfLastName.getText().trim())) {
            errorMessage.append("Фамилия должна быть на кириллице и с заглавной буквы!\n");
        }

        // Проверка Имени
        if (tfFirstName.getText() == null || tfFirstName.getText().trim().isEmpty()) {
            errorMessage.append("Не указано имя!\n");
        } else if (!isFioValid(tfFirstName.getText().trim())) {
            errorMessage.append("Имя должно быть на кириллице и с заглавной буквы!\n");
        }

        // Проверка Отчества (если оно введено)
        String middleName = tfMiddleName.getText();
        if (middleName != null && !middleName.trim().isEmpty()) {
            if (!isFioValid(middleName.trim())) {
                errorMessage.append("Отчество должно быть на кириллице и с заглавной буквы!\n");
            }
        }

        // Проверка Паспорта
        if (tfPassport.getText() == null || tfPassport.getText().trim().isEmpty()) {
            errorMessage.append("Не указаны паспортные данные!\n");
        } else if (!isPassportValid(tfPassport.getText().trim())) {
            errorMessage.append("Паспорт должен содержать ровно 10 цифр!\n");
        }

        // Проверка Года рождения
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

        // Если ошибок нет - пропускаем
        if (errorMessage.isEmpty()) {
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

    // ВАЛИДАТОРЫ
    private boolean isFioValid(String fioPart) {
        return fioPart != null && fioPart.matches("^[А-ЯЁ][а-яё]*(-[А-ЯЁ][а-яё]*)?$");
    }

    private boolean isPassportValid(String passport) {
        // Т.к. маска ставит пробел, регулярка ожидает: 4 цифры, пробел, 6 цифр
        return passport != null && passport.matches("^\\d{4} \\d{6}$");
    }
}