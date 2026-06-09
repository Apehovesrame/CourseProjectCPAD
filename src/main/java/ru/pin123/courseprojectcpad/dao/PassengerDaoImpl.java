package ru.pin123.courseprojectcpad.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Passenger;
import ru.pin123.courseprojectcpad.PropertiesUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PassengerDaoImpl {
    private static final Logger logger = LoggerFactory.getLogger(PassengerDaoImpl.class);
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
        // 1. ИЩЕМ ПАССАЖИРА ПО ПАСПОРТУ
        String selectSql = PropertiesUtil.get("sql.passenger.find_by_passport");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {

            pstmt.setString(1, passport);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // ПАССАЖИР НАЙДЕН!
                    // Проверяем, совпадают ли ФИО (без учета регистра)
                    String dbLastName = rs.getString("last_name");
                    String dbFirstName = rs.getString("first_name");

                    if (!dbLastName.equalsIgnoreCase(lastName) || !dbFirstName.equalsIgnoreCase(firstName)) {
                        // РЕШЕНИЕ ПРОБЛЕМЫ №2: Блокируем продажу, если паспорт чужой
                        logger.warn("Попытка оформления на чужой паспорт! Введено: {} {}, В базе: {} {} (Паспорт: {})",
                                lastName, firstName, dbLastName, dbFirstName, passport);
                        throw new RuntimeException("Этот паспорт (" + passport + ") уже зарегистрирован на другого пассажира: " + dbLastName + " " + dbFirstName);
                    }

                    // Если ФИО совпали, возвращаем пассажира из базы
                    Passenger p = new Passenger();
                    p.setPassengerId(rs.getLong("passenger_id"));
                    p.setLastName(dbLastName);
                    p.setFirstName(dbFirstName);
                    p.setMiddleName(rs.getString("middle_name"));
                    p.setPassportNumber(rs.getString("passport_number"));
                    p.setBirthYear(rs.getInt("birth_year"));
                    return p;
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пассажира по паспорту", e);
            throw new RuntimeException("Ошибка БД при проверке паспорта", e);
        }

        // 2. ЕСЛИ НЕ НАЙДЕН — СОЗДАЕМ НОВОГО И ЗАПИСЫВАЕМ В БД (РЕШЕНИЕ ПРОБЛЕМЫ №1)
        String insertSql = PropertiesUtil.get("sql.passenger.insert");

        // Обязательно указываем Statement.RETURN_GENERATED_KEYS, чтобы получить ID созданного пассажира
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, lastName);
            pstmt.setString(2, firstName);
            pstmt.setString(3, middleName);
            pstmt.setString(4, passport);
            pstmt.setInt(5, birthYear);
            pstmt.executeUpdate();

            // Достаем сгенерированный базой данных ID
            try (ResultSet rsKeys = pstmt.getGeneratedKeys()) {
                if (rsKeys.next()) {
                    Passenger p = new Passenger();
                    p.setPassengerId(rsKeys.getLong(1)); // Сохраняем реальный ID из базы!
                    p.setLastName(lastName);
                    p.setFirstName(firstName);
                    p.setMiddleName(middleName);
                    p.setPassportNumber(passport);
                    p.setBirthYear(birthYear);

                    logger.info("В базу данных успешно добавлен новый пассажир: {} {} (ID: {})", lastName, firstName, p.getPassengerId());
                    return p;
                } else {
                    throw new RuntimeException("Сбой БД: не удалось получить ID нового пассажира.");
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при создании нового пассажира", e);
            throw new RuntimeException("Ошибка БД при сохранении нового пассажира", e);
        }
    }
}