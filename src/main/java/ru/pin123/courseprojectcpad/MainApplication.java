package ru.pin123.courseprojectcpad;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainApplication extends Application {

    // Создаем логгер для главного класса
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);
    public static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        MainApplication.stage = primaryStage;
        logger.info("Приложение запущено");

        // === ХИТРОСТЬ ДЛЯ TESTFX ===
        // TestFX жестко требует, чтобы primary stage был показан при старте.
        // Создаем временную пустую сцену-заглушку и показываем её до диалога авторизации.
        primaryStage.setScene(new Scene(new javafx.scene.layout.Pane(), 800, 600));
        primaryStage.setTitle("Авторизация...");
        primaryStage.show();

        // Асинхронный запуск логики, чтобы не блокировать процесс старта
        Platform.runLater(() -> {
            while (true) {
                LoginDialog loginDialog = new LoginDialog();
                Optional<LoginDialog.LoginResult> result = loginDialog.showAndWait();

                if (result.isEmpty()) {
                    logger.info("Пользователь отменил вход, выход");
                    return; // Выходим из метода (Platform.exit() здесь не пишем!)
                }

                String username = result.get().getUsername();
                String password = result.get().getPassword();

                try {
                    DBHelper.initConnection(username, password);
                    logger.info("Успешное подключение для {}", username);
                    break;
                } catch (SQLException ex) {
                    logger.error("Ошибка подключения для {}: {}", username, ex.getMessage());

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка подключения");
                    alert.setHeaderText("Не удалось подключиться к базе данных");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                }
            }

            // Успешный вход -> подменяем пустую сцену на настоящий интерфейс
            try {
                ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
                logger.info("Загружены ресурсы для локали {}", Locale.getDefault());

                FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/ru/pin123/courseprojectcpad/view/main-view.fxml"), bundle);

                // Просто меняем содержимое уже открытого окна
                primaryStage.setScene(new Scene(fxmlLoader.load(), 1100, 700));
                primaryStage.setTitle(bundle.getString("app.title"));

            } catch (IOException e) {
                logger.error("Ошибка загрузки FXML", e);
            }
        });
    }

    // Этот метод вызывается автоматически при закрытии программы
    @Override
    public void stop() throws Exception {
        DBHelper.closeConnection(); // Закрываем коннект к БД
        logger.info("Приложение завершило работу");
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}