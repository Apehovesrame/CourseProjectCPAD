package ru.pin123.courseprojectcpad.service;

import ru.pin123.courseprojectcpad.dao.BusDaoImpl;
import ru.pin123.courseprojectcpad.model.Bus;

import java.util.List;

public class BusService {
    private final BusDaoImpl busDao = new BusDaoImpl();

    public List<Bus> getAllBuses() {
        return busDao.findAll();
    }

    public void saveBus(Bus bus) {
        // Если ID нулевой — это новый автобус, иначе — редактирование старого
        if (bus.getBusId() == null || bus.getBusId() == 0) {
            busDao.save(bus);
        } else {
            busDao.update(bus);
        }
    }

    public void deleteBus(Long id) {
        busDao.delete(id);
    }
}