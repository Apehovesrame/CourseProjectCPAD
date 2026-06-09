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
    @FXML private ComboBox<Role> comboRole;
    @FXML private TextField tfLogin;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfLastName;
    @FXML private TextField tfFirstName;

    private Stage dialogStage;
    private User user;
    private boolean isOkClicked = false;
    private final UserDaoImpl userDao = new UserDaoImpl();

    public void setDialogStage(Stage dialogStage) { this.dialogStage = dialogStage; }
    public boolean isOkClicked() { return isOkClicked; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboRole.setItems(FXCollections.observableArrayList(userDao.findAllRoles()));
    }

    public void setUser(User user) {
        this.user = user;
        // Если это создание нового пользователя, пароль обязателен.
        // Если редактирование старого - поля заполнятся (пока реализуем только создание)
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            user.setRole(comboRole.getValue());
            user.setLogin(tfLogin.getText().trim());
            user.setLastName(tfLastName.getText().trim());
            user.setFirstName(tfFirstName.getText().trim());

            // Сохраняем в базу (пароль захешируется внутри DAO)
            try {
                userDao.save(user, pfPassword.getText());
                isOkClicked = true;
                dialogStage.close();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void handleCancel() { dialogStage.close(); }

    private boolean isInputValid() {
        if (comboRole.getValue() == null || tfLogin.getText().isEmpty() ||
                pfPassword.getText().isEmpty() || tfLastName.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Заполните все поля!").showAndWait();
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
}