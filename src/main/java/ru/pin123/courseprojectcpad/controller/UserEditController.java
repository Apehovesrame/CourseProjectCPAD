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

public class UserEditController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(UserEditController.class);

    @FXML private ComboBox<Role> comboRole;
    @FXML private TextField tfLogin;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfLastName;
    @FXML private TextField tfFirstName;

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
        logger.debug("Форма подготовки данных пользователя инициализирована.");
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            user.setRole(comboRole.getValue());
            user.setLogin(tfLogin.getText().trim());
            user.setLastName(tfLastName.getText().trim());
            user.setFirstName(tfFirstName.getText().trim());

            try {
                userDao.save(user, pfPassword.getText());
                isOkClicked = true;
                dialogStage.close();

                logger.info("Создан новый пользователь: Логин='{}', ФИО='{} {}', Роль='{}'.",
                        user.getLogin(), user.getLastName(), user.getFirstName(), user.getRole().getRoleName());
            } catch (Exception e) {
                logger.error("Ошибка при сохранении пользователя '{}' в базу данных.", user.getLogin(), e);
                // ИСПРАВЛЕНО: Локализован заголовок алерта
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
        if (comboRole.getValue() == null || tfLogin.getText().isEmpty() ||
                pfPassword.getText().isEmpty() || tfLastName.getText().isEmpty() || tfFirstName.getText().isEmpty()) {
            logger.warn("Валидация формы пользователя не пройдена: не заполнены обязательные поля.");
            // ИСПРАВЛЕНО: Локализация
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("alert.error.empty_fields"));
            return false;
        }

        if (!isFioValid(tfLastName.getText().trim())) {
            logger.warn("Валидация формы пользователя не пройдена: некорректный формат Фамилии ({}).", tfLastName.getText());
            // ИСПРАВЛЕНО: Локализация (ключ мы создавали для кассы)
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("sell.validation.fio"));
            return false;
        }

        if (!isFioValid(tfFirstName.getText().trim())) {
            logger.warn("Валидация формы пользователя не пройдена: некорректный формат Имени ({}).", tfFirstName.getText());
            // ИСПРАВЛЕНО: Локализация (тот же ключ)
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.warning.title"), resources.getString("sell.validation.fio"));
            return false;
        }

        return true;
    }

    private boolean isFioValid(String fioPart) {
        return fioPart != null && fioPart.matches("^[А-ЯЁ][а-яё]*(-[А-ЯЁ][а-яё]*)?$");
    }

    private boolean isPassportValid(String passport) {
        return passport != null && passport.matches("\\d{10}");
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