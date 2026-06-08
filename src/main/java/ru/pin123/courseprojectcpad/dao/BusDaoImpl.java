package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Bus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BusDaoImpl {

    public List<Bus> findAll() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT * FROM buses ORDER BY model";

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Bus bus = new Bus();
                bus.setBusId(rs.getLong("bus_id"));
                bus.setModel(rs.getString("model"));
                bus.setLicensePlate(rs.getString("license_plate"));
                bus.setSeatCapacity(rs.getInt("seat_capacity"));

                // КЛЮЧЕВОЙ МОМЕНТ: Чтение массива байт из колонки bus_image (тип bytea)
                bus.setBusImage(rs.getBytes("bus_image"));

                buses.add(bus);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке списка автобусов", e);
        }
        return buses;
    }

    public void save(Bus bus) {
        // Заменили photo_path на bus_image в INSERT
        String sql = "INSERT INTO buses (model, license_plate, seat_capacity, bus_image) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bus.getModel());
            pstmt.setString(2, bus.getLicensePlate());
            pstmt.setInt(3, bus.getSeatCapacity());

            // КЛЮЧЕВОЙ МОМЕНТ: Запись bytea с проверкой на null
            if (bus.getBusImage() != null) {
                pstmt.setBytes(4, bus.getBusImage());
            } else {
                pstmt.setNull(4, java.sql.Types.BINARY); // Безопасно пишем NULL в базу
            }

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении автобуса", e);
        }
    }

    public void update(Bus bus) {
        // Заменили photo_path на bus_image в UPDATE
        String sql = "UPDATE buses SET model = ?, license_plate = ?, seat_capacity = ?, bus_image = ? WHERE bus_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bus.getModel());
            pstmt.setString(2, bus.getLicensePlate());
            pstmt.setInt(3, bus.getSeatCapacity());

            // КЛЮЧЕВОЙ МОМЕНТ: Обновление bytea с проверкой на null
            if (bus.getBusImage() != null) {
                pstmt.setBytes(4, bus.getBusImage());
            } else {
                pstmt.setNull(4, java.sql.Types.BINARY);
            }

            pstmt.setLong(5, bus.getBusId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении автобуса", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM buses WHERE bus_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении автобуса", e);
        }
    }

    public Optional<Bus> findById(Long id) {
        String sql = "SELECT * FROM buses WHERE bus_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Bus bus = new Bus();
                    bus.setBusId(rs.getLong("bus_id"));
                    bus.setModel(rs.getString("model"));
                    bus.setLicensePlate(rs.getString("license_plate"));
                    bus.setSeatCapacity(rs.getInt("seat_capacity"));

                    // КЛЮЧЕВОЙ МОМЕНТ: Чтение массива байт в findById
                    bus.setBusImage(rs.getBytes("bus_image"));

                    return Optional.of(bus);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске автобуса по ID", e);
        }
        return Optional.empty();
    }
}