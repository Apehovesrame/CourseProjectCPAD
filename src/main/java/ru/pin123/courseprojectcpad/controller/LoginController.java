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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class LoginController {

    // ИСПРАВЛЕНО: Создали логгер для текущего класса
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;

    private final AuthService authService = new AuthService();

    @FXML
    public void onLoginClick(ActionEvent event) {
        // ИСПРАВЛЕНО: Подгружаем бандл локализации (из папки ресурсов)
        ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());

        try {
            String login = txtLogin.getText() != null ? txtLogin.getText().trim() : "";
            String password = txtPassword.getText() != null ? txtPassword.getText() : "";

            if (login.isEmpty() || password.isEmpty()) {
                logger.warn("Неудачная попытка входа: пустые поля ввода.");
                showAlert(Alert.AlertType.ERROR, bundle.getString("alert.error.title"), bundle.getString("alert.error.empty_fields"));
                return;
            }

            // Авторизуем пользователя
            authService.login(login, password);

            String userName = Session.getCurrentUser().getFirstName();

            // ИСПРАВЛЕНО: Логируем успешный вход сотрудника
            logger.info("Пользователь [{}] ({}) успешно вошел в систему.", login, userName);

            String welcomeMessage = bundle.getString("alert.success.welcome") + ", " + userName + "!";
            showAlert(Alert.AlertType.INFORMATION, bundle.getString("alert.success.title"), welcomeMessage);

            // --- МЕХАНИЗМ ПЕРЕКЛЮЧЕНИЯ ОКНА С ЛОКАЛИЗАЦИЕЙ ---

            // 1. Передаем bundle в FXMLLoader, чтобы главное окно открылось на нужном языке!
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/main-view.fxml"), bundle);
            Parent mainRoot = loader.load();

            // 2. Создаем новую сцену
            Stage stage = new Stage();
            stage.setTitle(bundle.getString("app.title")); // Заголовок окна из ресурсов
            stage.setScene(new Scene(mainRoot));
            stage.show();

            // 3. Закрываем текущее окно авторизации
            txtLogin.getScene().getWindow().hide();

        } catch (RuntimeException e) {
            // ИСПРАВЛЕНО: Логируем ошибку неверного пароля или проблемы БД
            logger.error("Ошибка авторизации для логина [{}]: {}", txtLogin.getText(), e.getMessage());
            showAlert(Alert.AlertType.ERROR, bundle.getString("alert.error.title"), e.getMessage());
        } catch (IOException e) {
            // ИСПРАВЛЕНО: Логируем критическую ошибку загрузки FXML файла
            logger.error("Критическая ошибка интерфейса при загрузке main-view.fxml", e);
            showAlert(Alert.AlertType.ERROR, bundle.getString("alert.error.title"), bundle.getString("alert.error.io_exception") + " " + e.getMessage());
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