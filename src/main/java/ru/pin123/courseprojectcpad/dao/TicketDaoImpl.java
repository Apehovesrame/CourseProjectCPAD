package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketDaoImpl implements TicketDao {

    @Override
    public void create(Ticket ticket) {
        String sql = "{? = CALL sell_ticket_func(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBHelper.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {

            // 1. Регистрируем первый параметр как выходной (это то, что вернет БД)
            cstmt.registerOutParameter(1, Types.BIGINT);

            // 2. Устанавливаем входные параметры (со 2 по 7 позицию)
            cstmt.setLong(2, ticket.getTrip().getTripId());
            cstmt.setLong(3, ticket.getPassenger().getPassengerId());
            cstmt.setLong(4, ticket.getDestinationStop().getStopId());
            cstmt.setLong(5, ticket.getSoldByUser().getUserId());
            cstmt.setInt(6, ticket.getSeatNumber());
            cstmt.setBigDecimal(7, ticket.getCost());

            // 3. Выполняем функцию (и триггеры логирования/проверки мест сработают автоматически!)
            cstmt.execute();

            // 4. Достаем сгенерированный ticket_id из первого параметра и кладем в объект
            long generatedId = cstmt.getLong(1);
            ticket.setTicketId(generatedId);

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при вызове функции продажи билета: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        String sql = "SELECT * FROM Tickets WHERE ticket_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTicket(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при поиске билета по ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Ticket> findAll() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM Tickets";
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка билетов", e);
        }
        return tickets;
    }

    @Override
    public List<Ticket> findTicketsByTripId(Long tripId) {
        List<Ticket> tickets = new ArrayList<>();
        // Этот метод нужен контроллеру, чтобы понять, какие места в автобусе заблокировать (уже проданы)
        String sql = "SELECT * FROM Tickets WHERE trip_id = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapResultSetToTicket(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении билетов для рейса " + tripId, e);
        }
        return tickets;
    }

    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setTicketId(rs.getLong("ticket_id"));
        ticket.setSeatNumber(rs.getInt("seat_number"));
        ticket.setCost(rs.getBigDecimal("cost"));

        // ResultSet.getTimestamp может вернуть null, поэтому лучше сделать проверку,
        // но если sale_date имеет DEFAULT CURRENT_TIMESTAMP, то всё окей.
        Timestamp saleDate = rs.getTimestamp("sale_date");
        if (saleDate != null) {
            ticket.setSaleDate(saleDate.toLocalDateTime());
        }

        // Внимание: В будущем сюда нужно будет добавить маппинг вложенных объектов
        // (Trip, Passenger, Stop, User), если они понадобятся при выводе на экран.

        return ticket;
    }
}