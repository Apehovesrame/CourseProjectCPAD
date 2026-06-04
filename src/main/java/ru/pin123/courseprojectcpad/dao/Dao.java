package ru.pin123.courseprojectcpad.dao;

import java.util.List;
import java.util.Optional;

// Обобщенный интерфейс для всех таблиц базы данных
public interface Dao<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    void save(T t);
    void update(T t);
    void delete(ID id);
}