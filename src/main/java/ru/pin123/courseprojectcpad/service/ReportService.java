package ru.pin123.courseprojectcpad.service;

import ru.pin123.courseprojectcpad.dao.ReportDaoImpl;
import ru.pin123.courseprojectcpad.model.ReportRow;

import java.time.LocalDate;
import java.util.List;

public class ReportService {

    private final ReportDaoImpl reportDao = new ReportDaoImpl();

    public List<ReportRow> generateRevenueReport(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Необходимо указать начальную и конечную дату.");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Начальная дата не может быть позже конечной.");
        }

        return reportDao.getRevenueReport(start, end);
    }
}