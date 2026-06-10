package ru.pin123.courseprojectcpad.util;

import javafx.application.Platform;
import javafx.scene.control.TextField;

/**
 * Утилитный класс для настройки валидации и форматирования полей ввода
 * в графическом интерфейсе приложения.
 */
public class UIValidationHelper {

    /**
     * Настраивает автоматическую маску ввода для паспортных данных.
     * Ограничивает длину до 10 цифр и форматирует строку в вид "0000 000000".
     *
     * @param textField текстовое поле (JavaFX), к которому применяется маска
     */
    public static void setupPassportMask(TextField textField) {
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
                Platform.runLater(textField::end);
            }
        });
    }

    /**
     * Валидирует часть ФИО (фамилию, имя или отчество).
     * Проверяет, что строка начинается с заглавной буквы и содержит только кириллицу (допускается дефис).
     *
     * @param fioPart часть ФИО для проверки.
     * @return true, если формат корректен, иначе false.
     */
    public static boolean isFioValid(String fioPart) {
        return fioPart != null && fioPart.matches("^[А-ЯЁ][а-яё]*(-[А-ЯЁ][а-яё]*)?$");
    }

    /**
     * Валидирует формат паспортных данных с учетом маски (4 цифры, пробел, 6 цифр).
     *
     * @param passport строка паспортных данных для проверки.
     * @return true, если формат корректен, иначе false.
     */
    public static boolean isPassportValid(String passport) {
        return passport != null && passport.matches("^\\d{4} \\d{6}$");
    }
}