package ru.pin123.courseprojectcpad;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

public class MainApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);
    public static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        MainApplication.stage = primaryStage;
        logger.info("Приложение запущено");

        try {
            // 1. Проверяем системное подключение к PostgreSQL при старте
            DBHelper.initSystemConnection();

            // 2. Загружаем окно авторизации из FXML
            ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/ru/pin123/courseprojectcpad/view/login-view.fxml"), bundle);

            primaryStage.setScene(new Scene(fxmlLoader.load()));
            primaryStage.setTitle(bundle.getString("app.title")); // Берет название "Система учета..." из properties
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            logger.error("Критическая ошибка при запуске приложения", e);
        }
    }

    @Override
    public void stop() throws Exception {
        DBHelper.closeConnection();
        logger.info("Приложение завершило работу");
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}