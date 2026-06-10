package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.dao.UserDaoImpl;
import ru.pin123.courseprojectcpad.model.Role;
import ru.pin123.courseprojectcpad.model.User;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Контроллер модального диалогового окна для создания и редактирования учетных записей сотрудников.
 */
public class UserEditController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(UserEditController.class);

    @FXML private ComboBox<Role> comboRole;
    @FXML private TextField tfLogin;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfLastName;
    @FXML private TextField tfFirstName;
    @FXML private TextField tfMiddleName;

    // Внедряем бандл ресурсов
    @FXML private ResourceBundle resources;

    private Stage dialogStage;
    private User user;
    private boolean isOkClicked = false;
    private final UserDaoImpl userDao = new UserDaoImpl();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return isOkClicked;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Сохраняем ресурсы для использования в алертах
        this.resources = resources;

        try {
            comboRole.setItems(FXCollections.observableArrayList(userDao.findAllRoles()));
            logger.info("Инициализация формы пользователя. Загружено {} ролей.", comboRole.getItems().size());
        } catch (Exception e) {
            logger.error("Критическая ошибка при загрузке списка ролей из БД.", e);
        }
    }

    public void setUser(User user) {
        this.user = user;

        // Если это РЕДАКТИРОВАНИЕ существующего пользователя (ID не null)
        if (user.getUserId() != null) {
            tfLogin.setText(user.getLogin());
            tfLogin.setDisable(true); // Запрещаем менять логин
            tfLastName.setText(user.getLastName());
            tfFirstName.setText(user.getFirstName());
            if (user.getMiddleName() != null) tfMiddleName.setText(user.getMiddleName());

            // Выбираем нужную роль в ComboBox
            comboRole.getItems().stream()
                    .filter(r -> r.getRoleId().equals(user.getRole().getRoleId()))
                    .findFirst()
                    .ifPresent(comboRole::setValue);
        }
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            user.setRole(comboRole.getValue());
            user.setLogin(tfLogin.getText().trim());
            user.setLastName(tfLastName.getText().trim());
            user.setFirstName(tfFirstName.getText().trim());
            user.setMiddleName(tfMiddleName.getText().trim().isEmpty() ? null : tfMiddleName.getText().trim());

            try {
                // Берем пароль только если он был введен
                String rawPassword = pfPassword.getText().isEmpty() ? null : pfPassword.getText();

                if (user.getUserId() == null) {
                    userDao.save(user, rawPassword); // Создание нового
                    logger.info("Создан новый пользователь: Логин='{}', ФИО='{} {}', Роль='{}'.",
                            user.getLogin(), user.getLastName(), user.getFirstName(), user.getRole().getRoleName());
                } else {
                    userDao.update(user, rawPassword); // Обновление старого
                    logger.info("Обновлен пользователь: Логин='{}', ФИО='{} {}', Роль='{}'.",
                            user.getLogin(), user.getLastName(), user.getFirstName(), user.getRole().getRoleName());
                }

                isOkClicked = true;
                dialogStage.close();

            } catch (Exception e) {
                logger.error("Ошибка при сохранении пользователя '{}' в базу данных.", user.getLogin(), e);
                showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        logger.debug("Создание/редактирование пользователя отменено.");
        dialogStage.close();
    }

    private boolean isInputValid() {
        boolean isNewUser = (user.getUserId() == null);

        // 1. Проверка общих обязательных полей (БЕЗ ПАРОЛЯ)
        if (comboRole.getValue() == null || tfLogin.getText().trim().isEmpty() ||
                tfLastName.getText().trim().isEmpty() || tfFirstName.getText().trim().isEmpty()) {
            logger.warn("Валидация формы пользователя не пройдена: не заполнены обязательные поля.");
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("alert.error.empty_fields"));
            return false;
        }

        // 2. Проверка пароля (ОБЯЗАТЕЛЕН ТОЛЬКО ДЛЯ НОВЫХ)
        if (isNewUser && pfPassword.getText().isEmpty()) {
            logger.warn("Валидация формы пользователя не пройдена: не задан пароль для нового сотрудника.");
            // Используем новый ключ локализации, добавьте его в .properties
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("user.validation.password_required"));
            return false;
        }

        // 3. Валидация ФИО
        if (!isFioValid(tfLastName.getText().trim())) {
            logger.warn("Валидация формы пользователя не пройдена: некорректный формат Фамилии ({}).", tfLastName.getText());
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("sell.validation.fio"));
            return false;
        }

        if (!isFioValid(tfFirstName.getText().trim())) {
            logger.warn("Валидация формы пользователя не пройдена: некорректный формат Имени ({}).", tfFirstName.getText());
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("sell.validation.fio"));
            return false;
        }

        String middle = tfMiddleName.getText().trim();
        if (!middle.isEmpty() && !isFioValid(middle)) {
            logger.warn("Валидация формы пользователя не пройдена: некорректный формат Отчества ({}).", middle);
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("sell.validation.fio"));
            return false;
        }

        return true;
    }

    private boolean isFioValid(String fioPart) {
        return fioPart != null && fioPart.matches("^[А-ЯЁ][а-яё]*(-[А-ЯЁ][а-яё]*)?$");
    }

    // Вспомогательный метод для удобного вывода Alert
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}