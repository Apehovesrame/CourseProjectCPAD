package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriverDaoImpl {

    public List<Driver> findAll() {
        List<Driver> drivers = new ArrayList<>();
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
                driver.setAge(rs.getInt("age"));
                driver.setPassport(rs.getString("passport"));
                drivers.add(driver);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке списка водителей", e);
        }
        return drivers;
    }

    public void save(Driver driver) {
        String sql = "INSERT INTO drivers (last_name, first_name, middle_name, age, passport) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, driver.getLastName());
            pstmt.setString(2, driver.getFirstName());
            pstmt.setString(3, driver.getMiddleName());
            pstmt.setInt(4, driver.getAge());
            pstmt.setString(5, driver.getPassport());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении водителя", e);
        }
    }

    public void update(Driver driver) {
        String sql = "UPDATE drivers SET last_name = ?, first_name = ?, middle_name = ?, age = ?, passport = ? WHERE driver_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, driver.getLastName());
            pstmt.setString(2, driver.getFirstName());
            pstmt.setString(3, driver.getMiddleName());
            pstmt.setInt(4, driver.getAge());
            pstmt.setString(5, driver.getPassport());
            pstmt.setLong(6, driver.getDriverId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении водителя", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM drivers WHERE driver_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении водителя (возможно, он привязан к рейсу)", e);
        }
    }
}