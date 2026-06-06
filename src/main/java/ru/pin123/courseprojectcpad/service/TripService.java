package ru.pin123.courseprojectcpad.service;

import ru.pin123.courseprojectcpad.dao.TripDao;
import ru.pin123.courseprojectcpad.dao.TripDaoImpl;
import ru.pin123.courseprojectcpad.model.Driver;
import ru.pin123.courseprojectcpad.model.Trip;

import java.util.List;

public class TripService {

    // Подключаем наш новый DAO
    private final TripDao tripDao = new TripDaoImpl();

    private static final int LONG_TRIP_THRESHOLD_MINUTES = 240;

    public void createTrip(Trip trip, List<Driver> selectedDrivers) {
        if (selectedDrivers == null || selectedDrivers.isEmpty()) {
            throw new IllegalArgumentException("На рейс должен быть назначен хотя бы один водитель.");
        }

        int duration = trip.getRoute().getDurationMinutes();

        if (duration >= LONG_TRIP_THRESHOLD_MINUTES) {
            if (selectedDrivers.size() < 2) {
                throw new IllegalArgumentException(
                        "Ошибка: Время в пути составляет " + duration + " мин. " +
                                "На дальние маршруты обязательно назначение не менее ДВУХ водителей!"
                );
            }
        }

        // Вызываем сохранение транзакции в базе данных!
        tripDao.create(trip, selectedDrivers);
    }
}