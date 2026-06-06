package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.model.Driver;
import ru.pin123.courseprojectcpad.model.Trip;

import java.util.List;
import java.util.Optional;

public interface TripDao {
    // Сохранение рейса вместе со списком водителей
    void create(Trip trip, List<Driver> drivers);

    Optional<Trip> findById(Long id);
    List<Trip> findAll();
}