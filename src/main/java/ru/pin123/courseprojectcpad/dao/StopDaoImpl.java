package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Stop;
import ru.pin123.courseprojectcpad.PropertiesUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StopDaoImpl {

    public List<Stop> findAll() {
        List<Stop> stops = new ArrayList<>();
        String sql = PropertiesUtil.get("sql.stop.find_all");

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Stop stop = new Stop();
                stop.setStopId(rs.getLong("stop_id"));
                stop.setName(rs.getString("name"));

                stops.add(stop);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке списка остановок", e);
        }
        return stops;
    }
}