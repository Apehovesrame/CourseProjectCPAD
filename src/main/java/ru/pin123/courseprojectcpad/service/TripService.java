package ru.pin123.courseprojectcpad.service;

import ru.pin123.courseprojectcpad.dao.Dao;
import ru.pin123.courseprojectcpad.model.Driver;
import ru.pin123.courseprojectcpad.model.Trip;

import java.util.List;

public class TripService {

    // В будущем здесь будет инициализация вашего TripDaoImpl
    // private final TripDao tripDao = new TripDaoImpl();

    // Порог в минутах, после которого рейс считается дальним (например, 4 часа = 240 минут)
    private static final int LONG_TRIP_THRESHOLD_MINUTES = 240;

    /**
     * Проверяет бизнес-правила и передает рейс в DAO для сохранения
     */
    public void createTrip(Trip trip, List<Driver> selectedDrivers) {

        // 1. Проверка требования: количество водителей
        if (selectedDrivers == null || selectedDrivers.isEmpty()) {
            throw new IllegalArgumentException("На рейс должен быть назначен хотя бы один водитель.");
        }

        int duration = trip.getRoute().getDurationMinutes();

        // Если рейс дальний (больше или равно 4 часам)
        if (duration >= LONG_TRIP_THRESHOLD_MINUTES) {
            if (selectedDrivers.size() < 2) {
                throw new IllegalArgumentException(
                        "Ошибка: Время в пути составляет " + duration + " мин. " +
                                "На дальние маршруты обязательно назначение не менее ДВУХ водителей!"
                );
            }
        }

        // 2. В будущем здесь можно добавить проверку пересечения расписаний
        // (чтобы один водитель не был назначен на два рейса одновременно)

        // 3. Отправка в DAO (закомментировано до создания слоя DAO для Trips)
        // tripDao.create(trip, selectedDrivers);
    }
}