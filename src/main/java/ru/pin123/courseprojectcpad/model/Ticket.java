package ru.pin123.courseprojectcpad.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ticket {
    private Long ticketId;

    private Trip trip;
    private Passenger passenger;
    private Stop destinationStop;
    private User soldByUser;

    private int seatNumber;
    private LocalDateTime saleDate;
    private BigDecimal cost;

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public Stop getDestinationStop() {
        return destinationStop;
    }

    public void setDestinationStop(Stop destinationStop) {
        this.destinationStop = destinationStop;
    }

    public User getSoldByUser() {
        return soldByUser;
    }

    public void setSoldByUser(User soldByUser) {
        this.soldByUser = soldByUser;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}

