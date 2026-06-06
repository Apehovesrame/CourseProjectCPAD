package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Passenger;

import java.sql.*;

public class PassengerDaoImpl {

    public Passenger getOrCreate(String lastName, String firstName, String middleName, String passportNumber) {
        String findSql = "SELECT * FROM passengers WHERE passport_number = ?";
        String insertSql = "INSERT INTO passengers (last_name, first_name, middle_name, passport_number, birth_year) VALUES (?, ?, ?, ?, 1990) RETURNING passenger_id";

        try (Connection conn = DBHelper.getConnection()) {
            // 1. Пытаемся найти пассажира
            try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
                findStmt.setString(1, passportNumber);
                ResultSet rs = findStmt.executeQuery();
                if (rs.next()) {
                    return new Passenger(
                            rs.getLong("passenger_id"),
                            rs.getString("last_name"),
                            rs.getString("first_name"),
                            rs.getString("middle_name"),
                            rs.getString("passport_number")
                    );
                }
            }

            // 2. Если не нашли, создаем нового
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, lastName);
                insertStmt.setString(2, firstName);
                insertStmt.setString(3, middleName);
                insertStmt.setString(4, passportNumber);

                ResultSet rs = insertStmt.executeQuery();
                if (rs.next()) {
                    return new Passenger(rs.getLong(1), lastName, firstName, middleName, passportNumber);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при работе с БД пассажиров: " + e.getMessage(), e);
        }
        return null;
    }
}
