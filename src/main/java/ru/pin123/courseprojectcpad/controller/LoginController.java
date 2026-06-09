package ru.pin123.courseprojectcpad.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
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

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;

    // Компонент для выбора языка
    @FXML private ComboBox<String> langSelector;

    // JavaFX автоматически внедрит сюда текущий файл локализации (.properties)
    @FXML private ResourceBundle resources;

    private final AuthService authService = new AuthService();

    /**
     * Инициализация контроллера. Вызывается автоматически после загрузки FXML.
     */
    @FXML
    public void initialize() {
        if (langSelector != null) {
            langSelector.setItems(FXCollections.observableArrayList("Русский", "English"));

            // Устанавливаем текущее значение в зависимости от локали ОС или прошлого выбора
            if (Locale.getDefault().getLanguage().equals("en")) {
                langSelector.setValue("English");
            } else {
                langSelector.setValue("Русский");
            }
        }
    }

    /**
     * Обработчик события изменения значения в ComboBox.
     */
    @FXML
    public void onLanguageChange(ActionEvent event) {
        String selected = langSelector.getValue();
        if ("English".equals(selected)) {
            Locale.setDefault(new Locale("en", "US"));
        } else {
            Locale.setDefault(new Locale("ru", "RU"));
        }

        // Перезагружаем текущее окно авторизации с новым языком
        try {
            ResourceBundle newBundle = ResourceBundle.getBundle("main", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/login-view.fxml"), newBundle);
            Parent root = loader.load();

            Stage stage = (Stage) txtLogin.getScene().getWindow();
            stage.setTitle(newBundle.getString("app.title")); // Обновляем заголовок окна
            stage.setScene(new Scene(root));

            logger.info("Язык окна авторизации изменен на: {}", selected);
        } catch (IOException e) {
            logger.error("Ошибка при перезагрузке окна авторизации для смены языка", e);
        }
    }

    @FXML
    public void onLoginClick(ActionEvent event) {
        try {
            String login = txtLogin.getText() != null ? txtLogin.getText().trim() : "";
            String password = txtPassword.getText() != null ? txtPassword.getText() : "";

            if (login.isEmpty() || password.isEmpty()) {
                logger.warn("Неудачная попытка входа: пустые поля ввода.");
                showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), resources.getString("alert.error.empty_fields"));
                return;
            }

            authService.login(login, password);

            String userName = Session.getCurrentUser().getFirstName();

            logger.info("Пользователь [{}] ({}) успешно вошел в систему.", login, userName);

            String welcomeMessage = resources.getString("alert.success.welcome") + ", " + userName + "!";
            showAlert(Alert.AlertType.INFORMATION, resources.getString("alert.info.title"), welcomeMessage);

            // --- МЕХАНИЗМ ПЕРЕКЛЮЧЕНИЯ ОКНА С ЛОКАЛИЗАЦИЕЙ ---

            // Передаем текущий resources (бандл) в главное окно
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/pin123/courseprojectcpad/view/main-view.fxml"), resources);
            Parent mainRoot = loader.load();

            // Создаем новую сцену
            Stage stage = new Stage();
            stage.setTitle(resources.getString("app.title")); // Заголовок окна из ресурсов
            stage.setScene(new Scene(mainRoot));
            stage.show();

            // Закрываем текущее окно авторизации
            txtLogin.getScene().getWindow().hide();

        } catch (RuntimeException e) {
            logger.error("Ошибка авторизации для логина [{}]: {}", txtLogin.getText(), e.getMessage());
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), e.getMessage());
        } catch (IOException e) {
            logger.error("Критическая ошибка интерфейса при загрузке main-view.fxml", e);
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.error.title"), "I/O Exception: " + e.getMessage());
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