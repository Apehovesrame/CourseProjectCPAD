package ru.pin123.courseprojectcpad;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBHelper {
    private static final Logger logger = LoggerFactory.getLogger(DBHelper.class);
    private static String dbUrlBase;
    private static String dbName;
    private static String dbUser;     // Системный пользователь БД
    private static String dbPassword; // Пароль системного пользователя БД

    static {
        // Безопасное чтение файла настроек из ресурсов (работает даже в скомпилированном JAR)
        try (InputStream is = DBHelper.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                Properties prop = new Properties();
                prop.load(is);
                dbUrlBase = prop.getProperty("db.url");
                dbName = prop.getProperty("db.name");
                dbUser = prop.getProperty("db.user");         // <-- Читаем логин БД
                dbPassword = prop.getProperty("db.password"); // <-- Читаем пароль БД
                logger.info("Загружены настройки подключения к БД из config.properties");
            } else {
                logger.error("Файл config.properties не найден!");
                Platform.exit();
            }
        } catch (Exception ex) {
            logger.error("Ошибка загрузки config.properties", ex);
            Platform.exit();
        }
    }

    public static void initSystemConnection() throws SQLException {
        String fullUrl = dbUrlBase + dbName;
        logger.info("Попытка системного подключения к {}", fullUrl);
        // Проверяем коннект один раз при старте
        try (Connection testConn = DriverManager.getConnection(fullUrl, dbUser, dbPassword)) {
            logger.info("Системное соединение с БД успешно установлено");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dbUser == null || dbPassword == null) {
            throw new SQLException("Учетные данные БД не загружены! Проверьте config.properties");
        }
        return DriverManager.getConnection(dbUrlBase + dbName, dbUser, dbPassword);
    }

    public static void closeConnection() {
        logger.info("Работа с базой данных завершена");
    }
}