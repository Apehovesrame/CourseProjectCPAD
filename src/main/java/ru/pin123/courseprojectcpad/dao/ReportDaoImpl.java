package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.ReportRow;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDaoImpl {

    public List<ReportRow> getRevenueReport(LocalDate startDate, LocalDate endDate) {
        List<ReportRow> report = new ArrayList<>();

        // SQL запрос объединяет таблицы и группирует продажи по маршрутам
        String sql = "SELECT " +
                "  r.route_number || ': ' || r.departure_point || ' - ' || r.destination_point AS route_name, " +
                "  COUNT(t.ticket_id) AS tickets_sold, " +
                "  SUM(t.cost) AS total_revenue " +
                "FROM tickets t " +
                "JOIN trips tr ON t.trip_id = tr.trip_id " +
                "JOIN routes r ON tr.route_id = r.route_id " +
                "WHERE DATE(t.sale_date) >= ? AND DATE(t.sale_date) <= ? " +
                "GROUP BY r.route_id, r.route_number, r.departure_point, r.destination_point " +
                "ORDER BY total_revenue DESC";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Переводим Java LocalDate в SQL Date
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow(
                            rs.getString("route_name"),
                            rs.getInt("tickets_sold"),
                            rs.getBigDecimal("total_revenue")
                    );
                    report.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при формировании отчета", e);
        }
        return report;
    }
}