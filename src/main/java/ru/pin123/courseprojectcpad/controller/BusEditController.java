package ru.pin123.courseprojectcpad.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Bus;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер модального диалогового окна для создания и редактирования параметров автобуса.
 * Обеспечивает валидацию пользовательского ввода, чтение локальных графических файлов
 * и их конвертацию в бинарный поток (массив байт) для последующего сохранения в СУБД.
 */
public class BusEditController {

    /**
     * Логгер SLF4J для фиксации процессов интерактивного редактирования и валидации данных.
     */
    private static final Logger logger = LoggerFactory.getLogger(BusEditController.class);

    @FXML private TextField tfModel;
    @FXML private TextField tfLicensePlate;
    @FXML private TextField tfSeatCapacity;
    @FXML private ImageView imgPreview;

    /**
     * Окно текущего диалога (Stage) для управления его жизненным циклом и привязки модальности.
     */
    private Stage dialogStage;

    /**
     * Редактируемый или создаваемый объект автобуса.
     */
    private Bus bus;

    /**
     * Флаг успешного сохранения изменений (нажатия кнопки ОК).
     */
    private boolean isOkClicked = false;

    /**
     * Буферный массив байт, временно хранящий загруженное изображение автобуса перед записью в модель.
     */
    private byte[] busImageBytes = null;

    /**
     * Устанавливает Stage для текущего диалогового окна.
     * * @param dialogStage контейнер окна.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Передает редактируемый объект автобуса в контроллер и инициализирует
     * поля ввода значениями его текущих атрибутов. Если у автобуса есть фото,
     * генерирует превью из массива байт.
     * * @param bus экземпляр класса Bus для редактирования.
     */
    public void setBus(Bus bus) {
        this.bus = bus;
        if (bus.getModel() != null) tfModel.setText(bus.getModel());
        if (bus.getLicensePlate() != null) tfLicensePlate.setText(bus.getLicensePlate());
        if (bus.getSeatCapacity() > 0) tfSeatCapacity.setText(String.valueOf(bus.getSeatCapacity()));

        if (bus.getBusImage() != null && bus.getBusImage().length > 0) {
            logger.debug("Инициализация формы редактирования: извлечено фото автобуса ({} байт).", bus.getBusImage().length);
            this.busImageBytes = bus.getBusImage();
            ByteArrayInputStream bis = new ByteArrayInputStream(busImageBytes);
            imgPreview.setImage(new Image(bis));
        } else {
            imgPreview.setImage(null);
        }
    }

    /**
     * Возвращает статус завершения диалога.
     * * @return true, если пользователь успешно подтвердил форму, иначе false.
     */
    public boolean isOkClicked() {
        return isOkClicked;
    }

    /**
     * Вызывает нативный диалог операционной системы FileChooser для выбора графического файла.
     * Считывает выбранный файл в массив байт и обновляет компонент предварительного просмотра.
     */
    @FXML
    private void handleChoosePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите фото автобуса");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            try {
                logger.debug("Пользователь выбрал локальный файл изображения: {}", file.getAbsolutePath());
                // Читаем все байты выбранного файла в массив
                this.busImageBytes = Files.readAllBytes(file.toPath());
                logger.info("Файл изображения успешно преобразован в byte[]. Размер буфера: {} байт.", this.busImageBytes.length);

                // Отображаем превью в интерфейсе напрямую из файла для скорости
                imgPreview.setImage(new Image(file.toURI().toString()));
            } catch (Exception e) {
                logger.error("Критический сбой дискового ввода-вывода при конвертации файла изображения.", e);
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
     * Обработчик подтверждения ввода. При прохождении валидации осуществляет
     * маппинг данных из текстовых компонентов формы и буфера изображения в поля модели.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            bus.setModel(tfModel.getText().trim());
            bus.setLicensePlate(tfLicensePlate.getText().trim());
            bus.setSeatCapacity(Integer.parseInt(tfSeatCapacity.getText().trim()));
            bus.setBusImage(busImageBytes);

            isOkClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Закрывает модальное диалоговое окно без сохранения внесенных изменений.
     */
    @FXML
    private void handleCancel() {
        logger.debug("Сессия редактирования автобуса прервана пользователем.");
        dialogStage.close();
    }

    /**
     * Производит валидацию текстовых строк ввода на соответствие типам данных,
     * ограничениям бизнес-логики и незаполненности.
     * * @return true, если все поля заполнены корректно, иначе false.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (tfModel.getText() == null || tfModel.getText().trim().isEmpty()) errorMessage += "Не указана марка/модель автобуса!\n";
        if (tfLicensePlate.getText() == null || tfLicensePlate.getText().trim().isEmpty()) errorMessage += "Не указан госномер!\n";

        if (tfSeatCapacity.getText() == null || tfSeatCapacity.getText().trim().isEmpty()) {
            errorMessage += "Не указана вместимость!\n";
        } else {
            try {
                int capacity = Integer.parseInt(tfSeatCapacity.getText().trim());
                if (capacity <= 0) errorMessage += "Вместимость должна быть больше нуля!\n";
            } catch (NumberFormatException e) {
                errorMessage += "Вместимость должна быть целым числом!\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            logger.warn("Форма редактирования не прошла валидацию. Причина:\n{}", errorMessage.trim());
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