package ru.pin123.courseprojectcpad;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBHelper {
    private static final Logger logger = LoggerFactory.getLogger(DBHelper.class);
    private static String dbUrlBase;
    private static String dbName;

    // Сохраняем учетные данные текущего пользователя сессии
    private static String savedUser;
    private static String savedPassword;

    static {
        URL url = DBHelper.class.getResource("/config.properties");
        if (url != null) {
            Properties prop = new Properties();
            try (FileInputStream fis = new FileInputStream(url.getFile())) {
                prop.load(fis);
                dbUrlBase = prop.getProperty("db.url");
                dbName = prop.getProperty("db.name");
                logger.debug("Загружены настройки подключения (url, name)");
            } catch (Exception ex) {
                logger.error("Ошибка загрузки config.properties", ex);
                Platform.exit();
            }
        } else {
            logger.error("Файл config.properties не найден!");
            Platform.exit();
        }
    }

    public static void initConnection(String user, String password) throws SQLException {
        savedUser = user;
        savedPassword = password;
        String fullUrl = dbUrlBase + dbName;

        logger.info("Тестовое подключение к {} пользователем {}", fullUrl, user);

        // Просто проверяем, что логин/пароль верные, и сразу закрываем
        try (Connection testConn = DriverManager.getConnection(fullUrl, user, password)) {
            logger.info("Соединение успешно установлено и проверено");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (savedUser == null || savedPassword == null) {
            throw new SQLException("Соединение не инициализировано. Вызовите initConnection()");
        }
        // ВАЖНО: Выдаем НОВОЕ соединение каждый раз, чтобы try-with-resources мог безопасно его закрыть
        return DriverManager.getConnection(dbUrlBase + dbName, savedUser, savedPassword);
    }

    public static void closeConnection() {
        // Больше ничего не делаем вручную, соединения закрываются автоматически в DAO
        logger.info("Очистка ресурсов БД завершена");
        savedUser = null;
        savedPassword = null;
    }
}