package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Route;
import ru.pin123.courseprojectcpad.PropertiesUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteDaoImpl {

    public List<Route> findAll() {
        List<Route> routes = new ArrayList<>();
        String sql = PropertiesUtil.get("sql.route.find_all");
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Route route = new Route();
                route.setRouteId(rs.getLong("route_id"));
                route.setRouteNumber(rs.getString("route_number"));
                route.setDeparturePoint(rs.getString("departure_point"));
                route.setDestinationPoint(rs.getString("destination_point"));
                route.setDurationMinutes(rs.getInt("duration_minutes"));
                routes.add(route);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке списка маршрутов", e);
        }
        return routes;
    }

    public void save(Route route) {
        String sql = PropertiesUtil.get("sql.route.insert");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, route.getRouteNumber());
            pstmt.setString(2, route.getDeparturePoint());
            pstmt.setString(3, route.getDestinationPoint());
            pstmt.setInt(4, route.getDurationMinutes());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении маршрута", e);
        }
    }

    public void update(Route route) {
        String sql = PropertiesUtil.get("sql.route.update");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, route.getRouteNumber());
            pstmt.setString(2, route.getDeparturePoint());
            pstmt.setString(3, route.getDestinationPoint());
            pstmt.setInt(4, route.getDurationMinutes());
            pstmt.setLong(5, route.getRouteId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при обновлении маршрута", e);
        }
    }

    public void delete(Long id) {
        String sql = PropertiesUtil.get("sql.route.delete");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении маршрута (возможно, на него уже есть рейсы)", e);
        }
    }
}