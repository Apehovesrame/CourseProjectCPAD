package ru.pin123.courseprojectcpad.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Passenger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер модального диалогового окна для создания и редактирования данных пассажира.
 * Отвечает за валидацию анкетных данных (ФИО, паспорт),
 * применение маски ввода для паспортных данных и синхронизацию состояния объекта модели с интерфейсом.
 */
public class PassengerEditController {

    /** Логгер для фиксации событий редактирования профиля пассажира. */
    private static final Logger logger = LoggerFactory.getLogger(PassengerEditController.class);

    /** Поле ввода фамилии пассажира. */
    @FXML private TextField tfLastName;
    /** Поле ввода имени пассажира. */
    @FXML private TextField tfFirstName;
    /** Поле ввода отчества пассажира. */
    @FXML private TextField tfMiddleName;
    /** Поле ввода паспортных данных (с автоматической маской). */
    @FXML private TextField tfPassport;

    /** Ссылка на модальное окно диалога. */
    private Stage dialogStage;
    /** Объект пассажира, данные которого редактируются. */
    private Passenger passenger;
    /** Флаг, указывающий на то, что пользователь подтвердил ввод данных (нажал ОК). */
    private boolean isOkClicked = false;

    /**
     * Инициализирует контроллер после загрузки FXML-файла.
     * Настраивает автоматическую маску ввода для поля паспортных данных.
     */
    @FXML
    public void initialize() {
        logger.debug("Инициализация формы редактирования пассажира. Настройка маски для поля паспорта.");
        ru.pin123.courseprojectcpad.util.UIValidationHelper.setupPassportMask(tfPassport);
    }

    /**
     * Устанавливает ссылку на модальное окно диалога.
     * @param dialogStage объект Stage для управления окном.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Инициализирует форму данными выбранного пассажира.
     * @param passenger объект Passenger для редактирования.
     */
    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
        if (passenger.getLastName() != null) tfLastName.setText(passenger.getLastName());
        if (passenger.getFirstName() != null) tfFirstName.setText(passenger.getFirstName());
        if (passenger.getMiddleName() != null) tfMiddleName.setText(passenger.getMiddleName());
        if (passenger.getPassportNumber() != null) tfPassport.setText(passenger.getPassportNumber());

        logger.debug("Форма заполнена данными пассажира: {} {} {}",
                passenger.getLastName(), passenger.getFirstName(), passenger.getMiddleName());
    }

    /**
     * Возвращает статус подтверждения данных пользователем.
     * @return true, если пользователь нажал кнопку ОК, иначе false.
     */
    public boolean isOkClicked() {
        return isOkClicked;
    }

    /**
     * Обрабатывает нажатие кнопки ОК. Валидирует введенные данные
     * и сохраняет их в объект модели, после чего закрывает диалоговое окно.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            passenger.setLastName(tfLastName.getText().trim());
            passenger.setFirstName(tfFirstName.getText().trim());
            passenger.setMiddleName(tfMiddleName.getText() != null ? tfMiddleName.getText().trim() : "");
            passenger.setPassportNumber(tfPassport.getText().trim());

            // Жестко передаем 0, так как мы отказались от сбора этого параметра
            passenger.setBirthYear(0);

            isOkClicked = true;
            dialogStage.close();

            logger.info("Данные пассажира успешно сохранены: {} {} (Паспорт: {}).",
                    passenger.getLastName(), passenger.getFirstName(), passenger.getPassportNumber());
        }
    }

    /**
     * Обрабатывает нажатие кнопки Отмена. Закрывает диалоговое окно без сохранения изменений.
     */
    @FXML
    private void handleCancel() {
        logger.debug("Редактирование пассажира отменено пользователем.");
        dialogStage.close();
    }

    /**
     * Проверяет корректность заполнения всех полей формы.
     * @return true, если данные валидны, иначе false.
     */
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

        // Если ошибок нет - пропускаем
        if (errorMessage.isEmpty()) {
            return true;
        } else {
            logger.warn("Валидация формы редактирования пассажира не пройдена.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Ошибка заполнения");
            alert.setHeaderText("Пожалуйста, исправьте ошибки:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
    }

    /**
     * Валидирует часть ФИО (фамилию, имя или отчество).
     * Проверяет, что строка начинается с заглавной буквы и содержит только кириллицу (допускается дефис).
     *
     * @param fioPart часть ФИО для проверки.
     * @return true, если формат корректен, иначе false.
     */
    private boolean isFioValid(String fioPart) {
        return fioPart != null && fioPart.matches("^[А-ЯЁ][а-яё]*(-[А-ЯЁ][а-яё]*)?$");
    }

    /**
     * Валидирует формат паспортных данных с учетом маски (4 цифры, пробел, 6 цифр).
     *
     * @param passport строка паспортных данных для проверки.
     * @return true, если формат корректен, иначе false.
     */
    private boolean isPassportValid(String passport) {
        // Т.к. маска ставит пробел, регулярка ожидает: 4 цифры, пробел, 6 цифр
        return passport != null && passport.matches("^\\d{4} \\d{6}$");
    }
}