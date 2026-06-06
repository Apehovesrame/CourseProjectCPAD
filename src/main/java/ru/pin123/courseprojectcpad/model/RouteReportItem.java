package ru.pin123.courseprojectcpad.model;

import java.math.BigDecimal;

public class RouteReportItem {
    private String destination;
    private int ticketsCount;
    private BigDecimal revenue;

    public RouteReportItem(String destination, int ticketsCount, BigDecimal revenue) {
        this.destination = destination;
        this.ticketsCount = ticketsCount;
        this.revenue = revenue;
    }

    public String getDestination() { return destination; }
    public int getTicketsCount() { return ticketsCount; }
    public BigDecimal getRevenue() { return revenue; }
}