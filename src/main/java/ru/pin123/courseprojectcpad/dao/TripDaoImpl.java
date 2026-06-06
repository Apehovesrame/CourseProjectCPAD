package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Driver;
import ru.pin123.courseprojectcpad.model.Trip;
import ru.pin123.courseprojectcpad.model.Route;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TripDaoImpl implements TripDao {

    @Override
    public void create(Trip trip, List<Driver> drivers) {
        String sqlTrip = "INSERT INTO trips (route_id, bus_id, created_by_user_id, departure_datetime, arrival_datetime, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, false)";
        String sqlDrivers = "INSERT INTO trips_drivers (trip_id, driver_id) VALUES (?, ?)";

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



    @Override
    public Optional<Trip> findById(Long id) {
        // Заглушка для будущего использования
        return Optional.empty();
    }

    @Override
    public List<Trip> findAll() {
        List<Trip> trips = new ArrayList<>();
        String sql = "SELECT t.*, r.route_number, r.departure_point, r.destination_point " +
                "FROM trips t " +
                "JOIN routes r ON t.route_id = r.route_id " +
                "WHERE t.is_deleted = false";

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Trip trip = new Trip();
                trip.setTripId(rs.getLong("trip_id"));

                Route route = new Route();
                route.setRouteId(rs.getLong("route_id"));
                route.setRouteNumber(rs.getString("route_number"));
                route.setDeparturePoint(rs.getString("departure_point"));
                route.setDestinationPoint(rs.getString("destination_point"));

                trip.setRoute(route);

                // ИСПРАВЛЕНИЕ: убираем .toString(), передаем LocalDateTime напрямую
                trip.setDepartureDatetime(rs.getTimestamp("departure_datetime").toLocalDateTime());

                trips.add(trip);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при загрузке списка рейсов", e);
        }
        return trips;
    }
}