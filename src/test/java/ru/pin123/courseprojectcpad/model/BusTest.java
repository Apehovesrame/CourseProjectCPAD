package ru.pin123.courseprojectcpad.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BusTest {

    @Test
    @DisplayName("Проверка вместимости автобуса")
    void testBusCapacity() {
        Bus bus = new Bus();
        bus.setSeatCapacity(45);
        bus.setModel("Mercedes-Benz");

        assertTrue(bus.getSeatCapacity() > 0, "Вместимость должна быть больше нуля");
        assertEquals(45, bus.getSeatCapacity());
        assertEquals("Mercedes-Benz", bus.getModel());
    }
}