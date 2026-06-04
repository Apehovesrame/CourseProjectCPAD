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
    private static Connection connection;
    private static String dbUrlBase;
    private static String dbName;

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
        if (connection != null && !connection.isClosed()) {
            closeConnection();
        }
        String fullUrl = dbUrlBase + dbName;
        logger.info("Подключение к {} пользователем {}", fullUrl, user);
        connection = DriverManager.getConnection(fullUrl, user, password);
        logger.info("Соединение установлено");
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Соединение не инициализировано. Вызовите initConnection()");
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Соединение закрыто");
            } catch (SQLException ex) {
                logger.error("Ошибка закрытия соединения", ex);
            }
        }
    }
}