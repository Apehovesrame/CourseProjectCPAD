package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.PropertiesUtil;
import ru.pin123.courseprojectcpad.model.Bus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BusDaoImpl implements Dao<Bus, Long> {

    @Override
    public List<Bus> findAll() {
        List<Bus> buses = new ArrayList<>();
        String sql = PropertiesUtil.get("sql.bus.find_all");

        try {
            Connection conn = DBHelper.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Bus bus = new Bus();
                    bus.setBusId(rs.getLong("bus_id"));
                    bus.setLicensePlate(rs.getString("license_plate"));
                    bus.setModel(rs.getString("model"));
                    bus.setSeatCapacity(rs.getInt("seat_capacity"));
                    buses.add(bus);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка автобусов: " + e.getMessage());
        }
        return buses;
    }

    @Override
    public void save(Bus bus) {
        String sql = PropertiesUtil.get("sql.bus.insert");

        try {
            Connection conn = DBHelper.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, bus.getLicensePlate());
                pstmt.setString(2, bus.getModel());
                pstmt.setInt(3, bus.getSeatCapacity());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении автобуса: " + e.getMessage());
        }
    }

    @Override
    public void update(Bus bus) {
        String sql = PropertiesUtil.get("sql.bus.update");
        try {
            Connection conn = DBHelper.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, bus.getLicensePlate());
                pstmt.setString(2, bus.getModel());
                pstmt.setInt(3, bus.getSeatCapacity());
                pstmt.setLong(4, bus.getBusId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении данных автобуса: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        String sql = PropertiesUtil.get("sql.bus.delete");
        try {
            Connection conn = DBHelper.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении автобуса: " + e.getMessage());
        }
    }

    @Override
    public Optional<Bus> findById(Long id) { return Optional.empty(); }
}