package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Passenger;
import ru.pin123.courseprojectcpad.PropertiesUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerDaoImpl {

    public List<Passenger> findAll() {
        List<Passenger> passengers = new ArrayList<>();
        String sql = PropertiesUtil.get("sql.passenger.find_all");
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Passenger p = new Passenger();
                p.setPassengerId(rs.getLong("passenger_id"));
                p.setLastName(rs.getString("last_name"));
                p.setFirstName(rs.getString("first_name"));
                p.setMiddleName(rs.getString("middle_name"));
                p.setPassportNumber(rs.getString("passport_number"));
                p.setBirthYear(rs.getInt("birth_year"));
                passengers.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке списка пассажиров", e);
        }
        return passengers;
    }

    public void save(Passenger passenger) {
        String sql = PropertiesUtil.get("sql.passenger.insert");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, passenger.getLastName());
            pstmt.setString(2, passenger.getFirstName());
            pstmt.setString(3, passenger.getMiddleName());
            pstmt.setString(4, passenger.getPassportNumber());
            pstmt.setInt(5, passenger.getBirthYear());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении пассажира", e);
        }
    }

    public void update(Passenger passenger) {
        String sql = PropertiesUtil.get("sql.passenger.update");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, passenger.getLastName());
            pstmt.setString(2, passenger.getFirstName());
            pstmt.setString(3, passenger.getMiddleName());
            pstmt.setString(4, passenger.getPassportNumber());
            pstmt.setInt(5, passenger.getBirthYear());
            pstmt.setLong(6, passenger.getPassengerId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении пассажира", e);
        }
    }

    public void delete(Long id) {
        String sql = PropertiesUtil.get("sql.passenger.delete");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении пассажира", e);
        }
    }

    public Passenger getOrCreate(String lastName, String firstName, String middleName, String passport, int birthYear) {
        String findSql = PropertiesUtil.get("sql.passenger.find_by_passport");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findSql)) {

            findStmt.setString(1, passport);
            try (ResultSet rs = findStmt.executeQuery()) {
                if (rs.next()) {
                    Passenger p = new Passenger();
                    p.setPassengerId(rs.getLong("passenger_id"));
                    p.setLastName(rs.getString("last_name"));
                    p.setFirstName(rs.getString("first_name"));
                    p.setMiddleName(rs.getString("middle_name"));
                    p.setPassportNumber(rs.getString("passport_number"));
                    p.setBirthYear(rs.getInt("birth_year"));
                    return p;
                }
            }

            Passenger newPassenger = new Passenger();
            newPassenger.setLastName(lastName);
            newPassenger.setFirstName(firstName);
            newPassenger.setMiddleName(middleName);
            newPassenger.setPassportNumber(passport);
            newPassenger.setBirthYear(birthYear);

            String insertSql = PropertiesUtil.get("sql.passenger.insert");
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, lastName);
                insertStmt.setString(2, firstName);
                insertStmt.setString(3, middleName);
                insertStmt.setString(4, passport);
                insertStmt.setInt(5, birthYear);
                insertStmt.executeUpdate();

                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newPassenger.setPassengerId(generatedKeys.getLong(1));
                    }
                }
            }
            return newPassenger;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске/создании пассажира", e);
        }
    }
}