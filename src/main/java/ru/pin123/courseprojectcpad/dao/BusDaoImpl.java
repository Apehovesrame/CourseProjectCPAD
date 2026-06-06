package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Bus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BusDaoImpl { // Если у вас есть интерфейс BusDao, добавьте "implements BusDao"

    /**
     * Получить все автобусы из базы (для списков выбора и таблиц)
     */
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

                buses.add(bus);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке списка автобусов", e);
        }
        return buses;
    }

    public void save(Bus bus) {
        String sql = "INSERT INTO buses (model, license_plate, seat_capacity) VALUES (?, ?, ?)";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bus.getModel());
            pstmt.setString(2, bus.getLicensePlate());
            pstmt.setInt(3, bus.getSeatCapacity());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении автобуса", e);
        }
    }

    public void update(Bus bus) {
        String sql = "UPDATE buses SET model = ?, license_plate = ?, seat_capacity = ? WHERE bus_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bus.getModel());
            pstmt.setString(2, bus.getLicensePlate());
            pstmt.setInt(3, bus.getSeatCapacity());
            pstmt.setLong(4, bus.getBusId());
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
                    return Optional.of(bus);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске автобуса по ID", e);
        }
        return Optional.empty();
    }
}