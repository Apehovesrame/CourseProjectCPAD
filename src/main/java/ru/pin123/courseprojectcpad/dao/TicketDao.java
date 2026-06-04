package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.model.Ticket;

import java.util.List;
import java.util.Optional;


public interface TicketDao {


    void create(Ticket ticket);

    Optional<Ticket> findById(Long id);


    List<Ticket> findAll();

    List<Ticket> findTicketsByTripId(Long tripId);
}