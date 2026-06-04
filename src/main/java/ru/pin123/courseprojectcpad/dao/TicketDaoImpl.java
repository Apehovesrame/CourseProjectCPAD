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
        String sql = "INSERT INTO Tickets (trip_id, passenger_id, destination_stop_id, sold_by_user_id, seat_number, cost) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, ticket.getTrip().getTripId());
            pstmt.setLong(2, ticket.getPassenger().getPassengerId());
            pstmt.setLong(3, ticket.getDestinationStop().getStopId());
            pstmt.setLong(4, ticket.getSoldByUser().getUserId());
            pstmt.setInt(5, ticket.getSeatNumber());
            pstmt.setBigDecimal(6, ticket.getCost());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ticket.setTicketId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при сохранении билета", e);
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
        return new ArrayList<>();
    }

    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setTicketId(rs.getLong("ticket_id"));
        ticket.setSeatNumber(rs.getInt("seat_number"));
        ticket.setCost(rs.getBigDecimal("cost"));
        ticket.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());

        return ticket;
    }
}