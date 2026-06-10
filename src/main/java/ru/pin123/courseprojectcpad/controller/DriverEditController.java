package ru.pin123.courseprojectcpad.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Driver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер модального диалогового окна для создания и редактирования данных водителя.
 * Отвечает за валидацию анкетных данных, обработку загрузки изображений (конвертация в byte[]),
 * применение маски для паспортных данных и синхронизацию состояния объекта модели с интерфейсом.
 */
public class DriverEditController {

    /** Логгер для фиксации событий редактирования профиля водителя. */
    private static final Logger logger = LoggerFactory.getLogger(DriverEditController.class);

    @FXML private TextField tfLastName;
    @FXML private TextField tfFirstName;
    @FXML private TextField tfMiddleName;
    @FXML private TextField tfAge;
    @FXML private TextField tfPassport;
    @FXML private ImageView imgPreview;

    private Stage dialogStage;
    private Driver driver;
    private boolean isOkClicked = false;

    /** Буферный массив байт для временного хранения изображения водителя. */
    private byte[] driverImageBytes = null;

    /**
     * Инициализирует контроллер после загрузки FXML-файла.
     * Настраивает автоматическую маску ввода для поля паспортных данных.
     */
    @FXML
    public void initialize() {
        logger.debug("Инициализация формы редактирования водителя. Настройка маски для паспорта.");
        ru.pin123.courseprojectcpad.util.UIValidationHelper.setupPassportMask(tfPassport);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Инициализирует форму данными выбранного водителя.
     * @param driver объект Driver для редактирования.
     */
    public void setDriver(Driver driver) {
        this.driver = driver;
        if (driver.getLastName() != null) tfLastName.setText(driver.getLastName());
        if (driver.getFirstName() != null) tfFirstName.setText(driver.getFirstName());
        if (driver.getMiddleName() != null) tfMiddleName.setText(driver.getMiddleName());
        if (driver.getAge() > 0) tfAge.setText(String.valueOf(driver.getAge()));
        if (driver.getPassport() != null) tfPassport.setText(driver.getPassport());

        if (driver.getDriverImage() != null && driver.getDriverImage().length > 0) {
            logger.debug("Загрузка фото водителя из БД для отображения в превью.");
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

    /**
     * Вызывает файловый менеджер для выбора изображения и загружает его в память приложения.
     */
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
                this.driverImageBytes = Files.readAllBytes(file.toPath());
                logger.info("Изображение успешно загружено из файла: {}. Размер: {} байт.", file.getName(), driverImageBytes.length);
                imgPreview.setImage(new Image(file.toURI().toString()));
            } catch (Exception e) {
                logger.error("Критическая ошибка при чтении файла изображения водителя.", e);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(dialogStage);
                alert.setTitle("Ошибка чтения файла");
                alert.setHeaderText("Не удалось загрузить изображение");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * Сбрасывает текущее изображение водителя (удаление фото из профиля).
     */
    @FXML
    private void handleRemovePhoto() {
        this.driverImageBytes = null;
        imgPreview.setImage(null);
        logger.debug("Фотография водителя была удалена пользователем.");
    }

    /**
     * Валидирует данные и сохраняет их в объект модели при нажатии кнопки ОК.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            driver.setLastName(tfLastName.getText().trim());
            driver.setFirstName(tfFirstName.getText().trim());
            driver.setMiddleName(tfMiddleName.getText() != null ? tfMiddleName.getText().trim() : "");
            driver.setAge(Integer.parseInt(tfAge.getText().trim()));
            driver.setPassport(tfPassport.getText().trim());
            driver.setDriverImage(driverImageBytes);

            isOkClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Проверяет корректность заполнения всех полей формы.
     * @return true, если данные валидны, иначе false.
     */
    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (tfLastName.getText() == null || tfLastName.getText().trim().isEmpty() || !isFioValid(tfLastName.getText().trim()))
            errorMessage.append("Фамилия введена неверно (кириллица, заглавная буква)!\n");
        if (tfFirstName.getText() == null || tfFirstName.getText().trim().isEmpty() || !isFioValid(tfFirstName.getText().trim()))
            errorMessage.append("Имя введено неверно!\n");

        // Обновленная ошибка: учитываем пробел
        if (tfPassport.getText() == null || !isPassportValid(tfPassport.getText().trim()))
            errorMessage.append("Паспорт должен содержать ровно 10 цифр (формат: 1234 567890)!\n");

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
            logger.warn("Валидация формы редактирования водителя не пройдена.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Ошибка заполнения");
            alert.setHeaderText("Пожалуйста, исправьте ошибки:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
    }

    private boolean isFioValid(String fioPart) {
        return fioPart != null && fioPart.matches("^[А-ЯЁ][а-яё]*(-[А-ЯЁ][а-яё]*)?$");
    }

    /**
     * Валидирует формат паспортных данных с учетом маски (4 цифры, пробел, 6 цифр).
     */
    private boolean isPassportValid(String passport) {
        return passport != null && passport.matches("^\\d{4} \\d{6}$");
    }
}