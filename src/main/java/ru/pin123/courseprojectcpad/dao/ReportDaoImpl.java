package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.RouteReportItem;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDaoImpl {

    public List<RouteReportItem> getSalesReport(LocalDate startDate, LocalDate endDate) {
        List<RouteReportItem> report = new ArrayList<>();

        // SQL-запрос собирает пункт назначения, количество билетов и сумму (выручку)
        String sql = "SELECT r.destination_point, COUNT(t.ticket_id) as tickets_count, SUM(t.cost) as total_revenue " +
                "FROM tickets t " +
                "JOIN trips tr ON t.trip_id = tr.trip_id " +
                "JOIN routes r ON tr.route_id = r.route_id " +
                "WHERE DATE(tr.departure_datetime) >= ? AND DATE(tr.departure_datetime) <= ? " +
                "GROUP BY r.destination_point " +
                "ORDER BY tickets_count DESC";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String dest = rs.getString("destination_point");
                    int count = rs.getInt("tickets_count");
                    BigDecimal revenue = rs.getBigDecimal("total_revenue");
                    if (revenue == null) revenue = BigDecimal.ZERO;

                    report.add(new RouteReportItem(dest, count, revenue));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при формировании отчета", e);
        }
        return report;
    }
}