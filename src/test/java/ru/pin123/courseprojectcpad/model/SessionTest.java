package ru.pin123.courseprojectcpad.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SessionTest {

    @Test
    @DisplayName("Проверка сохранения и очистки сессии пользователя")
    void testSessionManagement() {
        User testUser = new User();
        testUser.setLogin("test_admin");

        // ИСПРАВЛЕНО для случая, если Role - это класс:
        Role adminRole = new Role();
        // В зависимости от того, как в твоем классе Role называется сеттер имени:
        // Например: adminRole.setName("ADMIN"); или adminRole.setRoleName("ADMIN");
        testUser.setRole(adminRole);

        // Имитируем вход в систему
        Session.setCurrentUser(testUser);

        assertNotNull(Session.getCurrentUser(), "Пользователь должен быть сохранен в сессии");
        assertEquals("test_admin", Session.getCurrentUser().getLogin(), "Логин в сессии должен совпадать");

        // Имитируем выход из системы
        Session.clear();

        assertNull(Session.getCurrentUser(), "После очистки Session.getCurrentUser() должен возвращать null");
    }
}