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
    private static String dbUrlBase, dbName, dbUser, dbPassword;

    static {
        try (InputStream is = DBHelper.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                Properties prop = new Properties();
                prop.load(is);
                dbUrlBase = prop.getProperty("db.url");
                dbName = prop.getProperty("db.name");
                dbUser = prop.getProperty("db.user");         // Добавь это в config.properties
                dbPassword = prop.getProperty("db.password"); // Добавь это в config.properties
            } else {
                Platform.exit();
            }
        } catch (Exception ex) {
            Platform.exit();
        }
    }

    public static void initSystemConnection() throws SQLException {
        try (Connection testConn = DriverManager.getConnection(dbUrlBase + dbName, dbUser, dbPassword)) {
            logger.info("Системное соединение установлено");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrlBase + dbName, dbUser, dbPassword);
    }

    public static void closeConnection() {
        logger.info("Очистка ресурсов БД завершена");
    }
}