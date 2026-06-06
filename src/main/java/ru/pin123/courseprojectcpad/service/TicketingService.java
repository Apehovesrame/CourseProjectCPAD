package ru.pin123.courseprojectcpad.service;

import ru.pin123.courseprojectcpad.dao.TicketDao;
import ru.pin123.courseprojectcpad.dao.TicketDaoImpl;
import ru.pin123.courseprojectcpad.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class TicketingService {

    private final TicketDao ticketDao;
ticketDao
    // В будущем здесь можно использовать Dependency Injection (например, Spring),
    // но пока создаем зависимость напрямую
    public TicketingService() {
        this.ticketDao = new TicketDaoImpl();
    }

    /**
     * Основной метод оформления билета
     */
    public Ticket sellTicket(Trip trip, Passenger passenger, Stop destination, User cashier, int seatNumber, BigDecimal cost) {

        // 1. Валидация на уровне бизнес-логики приложения
        if (seatNumber <= 0 || seatNumber > trip.getBus().getSeatCapacity()) {
            throw new IllegalArgumentException("Ошибка: Номер места превышает вместимость автобуса.");
        }
        if (cost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ошибка: Стоимость билета должна быть больше нуля.");
        }

        // 2. Сборка объекта
        Ticket ticket = new Ticket();
        ticket.setTrip(trip);
        ticket.setPassenger(passenger);
        ticket.setDestinationStop(destination);
        ticket.setSoldByUser(cashier);
        ticket.setSeatNumber(seatNumber);
        ticket.setCost(cost);

        // 3. Отправка в БД с умной обработкой ошибок
        try {
            ticketDao.create(ticket); // Здесь вызывается наша SQL функция и триггеры
            return ticket;
        } catch (RuntimeException e) {
            // Если сработает ваш триггер trg_check_capacity (место занято) или другая SQL-ошибка,
            // мы перехватываем её и отдаем контроллеру понятный текст, чтобы показать пользователю в окошке Alert
            String errorMessage = e.getMessage().toLowerCase();
            if (errorMessage.contains("capacity") || errorMessage.contains("место")) {
                throw new RuntimeException("Извините, это место уже занято. Пожалуйста, выберите другое.", e);
            }
            // Если ошибка другая — пробрасываем дальше
            throw new RuntimeException("Произошла системная ошибка при оформлении билета.", e);
        }
    }


    public List<Integer> getOccupiedSeats(Long tripId) {
        List<Ticket> soldTickets = ticketDao.findTicketsByTripId(tripId);

        // Превращаем список билетов просто в список номеров мест
        return soldTickets.stream()
                .map(Ticket::getSeatNumber)
                .collect(Collectors.toList());
    }
}