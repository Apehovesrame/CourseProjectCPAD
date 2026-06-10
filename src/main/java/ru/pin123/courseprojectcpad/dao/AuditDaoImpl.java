package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.model.AuditLog;
import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.PropertiesUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditDaoImpl {

    public List<AuditLog> findAll() {
        List<AuditLog> logs = new ArrayList<>();
        // Берем запрос из properties
        String sql = PropertiesUtil.get("sql.audit.find_all");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                AuditLog log = new AuditLog(
                        rs.getLong("log_id"),
                        rs.getString("table_name"),
                        rs.getString("action_type"),
                        rs.getString("old_data"),
                        rs.getString("new_data"),
                        rs.getString("db_user"),
                        rs.getTimestamp("changed_at") != null ? rs.getTimestamp("changed_at").toLocalDateTime() : null
                );
                logs.add(log);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении журнала аудита", e);
        }
        return logs;
    }
}