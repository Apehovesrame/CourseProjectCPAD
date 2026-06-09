package ru.pin123.courseprojectcpad.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PassengerTest {

    @Test
    @DisplayName("Создание пассажира и проверка заполнения полей")
    void testPassengerCreation() {
        Passenger p = new Passenger();
        p.setLastName("Иванов");
        p.setFirstName("Иван");
        p.setMiddleName("Иванович");
        p.setPassportNumber("1234 567890");
        p.setBirthYear(1995);

        assertAll("Проверка свойств пассажира",
                () -> assertEquals("Иванов", p.getLastName()),
                () -> assertEquals("Иван", p.getFirstName()),
                () -> assertEquals("Иванович", p.getMiddleName()),
                () -> assertEquals("1234 567890", p.getPassportNumber()),
                () -> assertEquals(1995, p.getBirthYear())
        );
    }
}