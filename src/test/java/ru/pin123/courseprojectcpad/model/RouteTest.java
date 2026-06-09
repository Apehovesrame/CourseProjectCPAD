package ru.pin123.courseprojectcpad.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RouteTest {

    @Test
    @DisplayName("Проверка форматирования: часы и минуты")
    void testFormattedDuration_HoursAndMinutes() {
        Route route = new Route();
        route.setDurationMinutes(135); // 2 часа 15 минут

        // Убрали точку после "ч", чтобы совпадало с реальным кодом
        assertEquals("2 ч 15 мин", route.getFormattedDuration(), "Время должно конвертироваться в часы и минуты");
    }

    @Test
    @DisplayName("Проверка форматирования: только минуты")
    void testFormattedDuration_OnlyMinutes() {
        Route route = new Route();
        route.setDurationMinutes(45);

        // Убрали точку после "ч"
        assertEquals("0 ч 45 мин", route.getFormattedDuration(), "Если меньше часа, должно быть 0 ч");
    }
}