package ru.pin123.courseprojectcpad;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream inputStream = PropertiesUtil.class.getResourceAsStream("/statements.properties")) {
            if (inputStream != null) {
                PROPERTIES.load(inputStream);
            } else {
                System.err.println("Файл statements.properties не найден!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки statements.properties", e);
        }
    }

    // Метод для получения SQL-запроса по ключу
    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }
}