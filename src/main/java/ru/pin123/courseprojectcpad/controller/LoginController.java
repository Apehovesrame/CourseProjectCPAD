package ru.pin123.courseprojectcpad.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.pin123.courseprojectcpad.model.Session;
import ru.pin123.courseprojectcpad.service.AuthService;

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

            // --- МЕХАНИЗМ ПЕРЕКЛЮЧЕНИЯ ОКНА (JavaFX) ---

            // 1. Загружаем файл главного окна из ресурсов
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/ru/pin123/courseprojectcpad/view/main-view.fxml")
            );
            javafx.scene.Parent mainRoot = loader.load();

            // 2. Создаем новую сцену
            javafx.scene.Stage stage = new javafx.scene.Stage();
            stage.setTitle("Система учета пассажироперевозок");
            stage.setScene(new javafx.scene.Scene(mainRoot));
            stage.show();

            // 3. Закрываем текущее окно авторизации
            txtLogin.getScene().getWindow().hide();

        } catch (IllegalArgumentException | RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка входа", e.getMessage());
        } catch (java.io.IOException e) {
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