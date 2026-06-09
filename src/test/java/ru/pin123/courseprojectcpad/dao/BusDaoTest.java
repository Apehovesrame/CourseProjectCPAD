package ru.pin123.courseprojectcpad.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.pin123.courseprojectcpad.model.Bus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BusDaoTest {

    // Если у тебя используется BusService, можно вызвать его, либо напрямую BusDaoImpl
    private final BusDaoImpl busDao = new BusDaoImpl();

    @Test
    @DisplayName("Интеграционный: Создание, поиск и удаление автобуса с фото (Blob/Bytea)")
    void testBusCrudWithImage() {
        String testPlate = "Т777ТТ33";

        // 1. Создаем тестовый автобус
        Bus bus = new Bus();
        bus.setModel("Тест-Марка");
        bus.setLicensePlate(testPlate);
        bus.setSeatCapacity(50);

        // Имитируем массив байт фотографии (например, 4 байта заглушки)
        byte[] mockPhoto = new byte[]{1, 2, 3, 4};
        bus.setBusImage(mockPhoto); // Проверь, как называется сеттер фото в твоей модели Bus (setBusImage или setImage)

        // Сохраняем в БД
        assertDoesNotThrow(() -> busDao.save(bus), "Сохранение автобуса должно проходить без ошибок");

        // 2. Ищем созданный автобус по номеру
        List<Bus> allBuses = busDao.findAll();
        Bus foundBus = allBuses.stream()
                .filter(b -> testPlate.equals(b.getLicensePlate()))
                .findFirst()
                .orElse(null);

        assertNotNull(foundBus, "Автобус должен быть успешно найден в базе данных");
        assertEquals("Тест-Марка", foundBus.getModel());
        assertNotNull(foundBus.getBusImage(), "Фотография автобуса должна успешно считываться из bytea/blob");

        // 3. Удаляем за собой тестовый автобус
        assertDoesNotThrow(() -> busDao.delete(foundBus.getBusId()), "Удаление автобуса должно проходить без ошибок");
    }
}