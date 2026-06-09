package ru.pin123.courseprojectcpad.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class TicketTest {

    @Test
    @DisplayName("Проверка свойств билета (цена и место)")
    void testTicketProperties() {
        Ticket ticket = new Ticket();
        ticket.setSeatNumber(12);
        // Используем BigDecimal, так как это правильный тип для денег в Java
        ticket.setCost(new BigDecimal("1550.50"));

        assertEquals(12, ticket.getSeatNumber());
        assertEquals(new BigDecimal("1550.50"), ticket.getCost(), "Стоимость билета должна сохраняться точно");
    }
}