package ru.pin123.courseprojectcpad.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.pin123.courseprojectcpad.model.Passenger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PassengerDaoTest {

    private final PassengerDaoImpl passengerDao = new PassengerDaoImpl();

    @Test
    @DisplayName("Интеграционный: Создание, поиск и удаление пассажира (CRUD)")
    void testPassengerCrudOperations() {
        String testPassport = "0000 000000";

        // 1. Создаем тестового пассажира
        Passenger newPassenger = passengerDao.getOrCreate("Тест", "Тест", "Тестович", testPassport, 1990);

        // Убеждаемся, что база данных успешно присвоила ему ID
        assertNotNull(newPassenger.getPassengerId(), "ID пассажира должен быть сгенерирован БД");

        // 2. Ищем его в базе
        List<Passenger> allPassengers = passengerDao.findAll();
        boolean isFound = allPassengers.stream()
                .anyMatch(p -> p.getPassportNumber().equals(testPassport));

        assertTrue(isFound, "Тестовый пассажир должен находиться в списке всех пассажиров");

        // 3. Удаляем за собой тестовые данные
        assertDoesNotThrow(() -> passengerDao.delete(newPassenger.getPassengerId()),
                "Удаление пассажира не должно вызывать ошибок");

        // 4. Проверяем, что удаление прошло успешно
        boolean isFoundAfterDelete = passengerDao.findAll().stream()
                .anyMatch(p -> p.getPassportNumber().equals(testPassport));
        assertFalse(isFoundAfterDelete, "Пассажир должен быть удален из базы");
    }
}