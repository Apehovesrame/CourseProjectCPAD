package ru.pin123.courseprojectcpad.service;

import ru.pin123.courseprojectcpad.model.Ticket;

import java.util.List;

public interface TicketingService {


    Ticket sellTicket(Long tripId, Long passengerId, Long destinationStopId, Long soldByUserId, int seatNumber);


    void returnTicket(Long ticketId);


    List<Integer> getAvailableSeats(Long tripId);
}