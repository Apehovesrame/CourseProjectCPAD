package ru.pin123.courseprojectcpad.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.pin123.courseprojectcpad.model.Session;
import ru.pin123.courseprojectcpad.service.AuthService;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;

    private final AuthService authService = new AuthService();

    @FXML
    public void onLoginClick(ActionEvent event) {
        try {
            String login = txtLogin.getText();
            String password = txtPassword.getText();

            // Авторизуем пользователя
            authService.login(login, password);

            String userName = Session.getCurrentUser().getFirstName();
            showAlert(Alert.AlertType.INFORMATION, "Успех", "Добро пожаловать, " + userName + "!");

            // --- МЕХАНИЗМ ПЕРЕКЛЮЧЕНИЯ ОКНА ---

            // 1. Загружаем файл главного окна
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/main-view.fxml"));
            Parent mainRoot = loader.load();

            // 2. Создаем новую сцену
            Stage stage = new Stage();
            stage.setTitle("Система учета пассажироперевозок");
            stage.setScene(new Scene(mainRoot));
            stage.show();

            // 3. Закрываем текущее окно авторизации
            txtLogin.getScene().getWindow().hide();

        } catch (RuntimeException e) {
            // RuntimeException перехватывает и IllegalArgumentException, и другие ошибки времени выполнения
            showAlert(Alert.AlertType.ERROR, "Ошибка входа", e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка интерфейса", "Не удалось загрузить главное окно: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}