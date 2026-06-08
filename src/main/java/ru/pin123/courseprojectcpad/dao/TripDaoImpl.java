package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Driver;
import ru.pin123.courseprojectcpad.model.Trip;
import ru.pin123.courseprojectcpad.model.Route;
import ru.pin123.courseprojectcpad.model.Bus;
import ru.pin123.courseprojectcpad.PropertiesUtil; // Убедились, что утилита импортирована

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TripDaoImpl implements TripDao {

    @Override
    public void create(Trip trip, List<Driver> drivers) {
        // ИСПРАВЛЕНО: Загружаем SQL-запросы из файла statements.properties по ключам
        String sqlTrip = PropertiesUtil.get("sql.trip.insert_trip");
        String sqlDrivers = PropertiesUtil.get("sql.trip.insert_driver");

        // Открываем соединение
        try (Connection conn = DBHelper.getConnection()) {

            // 1. ОТКЛЮЧАЕМ АВТОМАТИЧЕСКИЙ КОММИТ (Начинаем транзакцию)
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtTrip = conn.prepareStatement(sqlTrip, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement pstmtDrivers = conn.prepareStatement(sqlDrivers)) {

                // Шаг 1: Вставляем сам рейс
                pstmtTrip.setLong(1, trip.getRoute().getRouteId());
                pstmtTrip.setLong(2, trip.getBus().getBusId());
                pstmtTrip.setLong(3, trip.getCreatedByUser().getUserId());
                pstmtTrip.setTimestamp(4, Timestamp.valueOf(trip.getDepartureDatetime()));
                pstmtTrip.setTimestamp(5, Timestamp.valueOf(trip.getArrivalDatetime()));

                pstmtTrip.executeUpdate();

                // Шаг 2: Получаем сгенерированный базой trip_id
                long newTripId;
                try (ResultSet rs = pstmtTrip.getGeneratedKeys()) {
                    if (rs.next()) {
                        newTripId = rs.getLong(1);
                        trip.setTripId(newTripId);
                    } else {
                        throw new SQLException("Не удалось получить ID нового рейса.");
                    }
                }

                // Шаг 3: Вставляем водителей в таблицу-связку (Batch Insert - пакетная вставка)
                for (Driver driver : drivers) {
                    pstmtDrivers.setLong(1, newTripId);
                    pstmtDrivers.setLong(2, driver.getDriverId());
                    pstmtDrivers.addBatch(); // Добавляем в пакет
                }
                pstmtDrivers.executeBatch(); // Выполняем весь пакет за один раз

                // 2. ЕСЛИ ВСЁ ПРОШЛО УСПЕШНО — ФИКСИРУЕМ ТРАНЗАКЦИЮ
                conn.commit();

            } catch (SQLException e) {
                // 3. ЕСЛИ ГДЕ-ТО ОШИБКА — ОТКАТЫВАЕМ ВСЕ ИЗМЕНЕНИЯ НАЗАД
                conn.rollback();
                throw new RuntimeException("Ошибка при сохранении рейса: транзакция отменена", e);
            } finally {
                // Возвращаем настройки соединения как было
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка подключения к базе данных", e);
        }
    }

    public void update(Trip trip, List<Driver> drivers) {
        // ИСПРАВЛЕНО: SQL-запросы вынесены во внешний файл конфигурации
        String sqlTrip = PropertiesUtil.get("sql.trip.update_trip");
        String sqlDelDrivers = PropertiesUtil.get("sql.trip.delete_drivers");
        String sqlInsDrivers = PropertiesUtil.get("sql.trip.insert_driver");

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false); // Начинаем транзакцию

            try (PreparedStatement pstmtTrip = conn.prepareStatement(sqlTrip);
                 PreparedStatement pstmtDel = conn.prepareStatement(sqlDelDrivers);
                 PreparedStatement pstmtIns = conn.prepareStatement(sqlInsDrivers)) {

                // 1. Обновляем рейс
                pstmtTrip.setLong(1, trip.getRoute().getRouteId());
                pstmtTrip.setLong(2, trip.getBus().getBusId());
                pstmtTrip.setTimestamp(3, Timestamp.valueOf(trip.getDepartureDatetime()));
                pstmtTrip.setTimestamp(4, Timestamp.valueOf(trip.getArrivalDatetime()));
                pstmtTrip.setLong(5, trip.getTripId());
                pstmtTrip.executeUpdate();

                // 2. Удаляем старых привязанных водителей
                pstmtDel.setLong(1, trip.getTripId());
                pstmtDel.executeUpdate();

                // 3. Добавляем новых водителей
                for (Driver driver : drivers) {
                    pstmtIns.setLong(1, trip.getTripId());
                    pstmtIns.setLong(2, driver.getDriverId());
                    pstmtIns.addBatch();
                }
                pstmtIns.executeBatch();

                conn.commit(); // Фиксируем изменения
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Ошибка при обновлении рейса", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка БД", e);
        }
    }

    public void delete(Long tripId) {
        // ИСПРАВЛЕНО: Вынесли мягкое удаление в statements.properties
        String sql = PropertiesUtil.get("sql.trip.delete_trip");
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, tripId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при удалении рейса", e);
        }
    }

    @Override
    public Optional<Trip> findById(Long id) {
        // Заглушка для будущего использования
        return Optional.empty();
    }

    @Override
    public List<Trip> findAll() {
        List<Trip> trips = new ArrayList<>();
        // ИСПРАВЛЕНО: Сложный запрос JOIN загружается по ключу динамически
        String sql = PropertiesUtil.get("sql.trip.find_all");

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Trip trip = new Trip();
                trip.setTripId(rs.getLong("trip_id"));

                // 1. Собираем Маршрут
                Route route = new Route();
                route.setRouteId(rs.getLong("route_id"));
                route.setRouteNumber(rs.getString("route_number"));
                route.setDeparturePoint(rs.getString("departure_point"));
                route.setDestinationPoint(rs.getString("destination_point"));
                trip.setRoute(route);

                // 2. Собираем Автобус (Критично для отрисовки мест!)
                Bus bus = new Bus();
                bus.setBusId(rs.getLong("bus_id"));
                bus.setModel(rs.getString("model"));
                bus.setLicensePlate(rs.getString("license_plate"));
                bus.setSeatCapacity(rs.getInt("seat_capacity"));
                trip.setBus(bus);

                // 3. Даты
                trip.setDepartureDatetime(rs.getTimestamp("departure_datetime").toLocalDateTime());
                trip.setArrivalDatetime(rs.getTimestamp("arrival_datetime").toLocalDateTime());

                trips.add(trip);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке списка рейсов", e);
        }
        return trips;
    }
}