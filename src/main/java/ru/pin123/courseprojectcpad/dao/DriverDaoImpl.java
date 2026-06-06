package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriverDaoImpl {

    public List<Driver> findAll() {
        List<Driver> drivers = new ArrayList<>();
        // Сортируем по фамилии и имени для удобного поиска в списке
        String sql = "SELECT * FROM drivers ORDER BY last_name, first_name";

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Driver driver = new Driver();
                driver.setDriverId(rs.getLong("driver_id"));
                driver.setLastName(rs.getString("last_name"));
                driver.setFirstName(rs.getString("first_name"));
                driver.setMiddleName(rs.getString("middle_name"));

                drivers.add(driver);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке списка водителей", e);
        }
        return drivers;
    }
}