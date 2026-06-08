package ru.pin123.courseprojectcpad.dao;

import ru.pin123.courseprojectcpad.DBHelper;
import ru.pin123.courseprojectcpad.model.Ticket;
import ru.pin123.courseprojectcpad.PropertiesUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketDaoImpl implements TicketDao {

    @Override
    public void create(Ticket ticket) {
        String sql = PropertiesUtil.get("sql.ticket.sell");

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // ИСПРАВЛЕНО: Преобразуем Long в int с помощью .intValue(), так как в БД тип колонок - integer
            pstmt.setInt(1, ticket.getTrip().getTripId().intValue());
            pstmt.setInt(2, ticket.getPassenger().getPassengerId().intValue());

            // Небольшая защита: если остановка не выбрана, отправляем NULL
            if (ticket.getDestinationStop() != null && ticket.getDestinationStop().getStopId() != null) {
                pstmt.setInt(3, ticket.getDestinationStop().getStopId().intValue());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setInt(4, ticket.getSoldByUser().getUserId().intValue());
            pstmt.setInt(5, ticket.getSeatNumber());
            pstmt.setBigDecimal(6, ticket.getCost());

            // Выполняем функцию и забираем сгенерированный ticket_id
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ticket.setTicketId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка БД при продаже: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        String sql = PropertiesUtil.get("sql.ticket.find_by_id");
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
        String sql = PropertiesUtil.get("sql.ticket.find_all");
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
        String sql = PropertiesUtil.get("sql.ticket.find_by_trip_id");
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