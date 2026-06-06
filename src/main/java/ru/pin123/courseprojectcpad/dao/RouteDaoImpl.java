package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Route;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteDaoImpl {

    public List<Route> findAll() {
        List<Route> routes = new ArrayList<>();
        // Сортируем маршруты по названию для удобства кассира
        String sql = "SELECT * FROM routes ORDER BY departure_point, destination_point";

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
}