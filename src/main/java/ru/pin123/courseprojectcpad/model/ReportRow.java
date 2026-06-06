package ru.pin123.courseprojectcpad.model;

import java.math.BigDecimal;

public class ReportRow {
    private String routeName;
    private int ticketsSold;
    private BigDecimal totalRevenue;

    public ReportRow(String routeName, int ticketsSold, BigDecimal totalRevenue) {
        this.routeName = routeName;
        this.ticketsSold = ticketsSold;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    // Обязательно нужны Getters для JavaFX TableView, иначе столбцы будут пустыми!
    public String getRouteName() { return routeName; }
    public int getTicketsSold() { return ticketsSold; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
}