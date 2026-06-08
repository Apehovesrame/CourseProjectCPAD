package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.RouteReportItem;
import ru.pin123.courseprojectcpad.PropertiesUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDaoImpl {

    public List<RouteReportItem> getSalesReport(LocalDate startDate, LocalDate endDate) {
        List<RouteReportItem> report = new ArrayList<>();

        String sql = PropertiesUtil.get("sql.report.sales");

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