package dk.easv.seaticketsystem.BLL;

import dk.easv.seaticketsystem.DAL.TicketRepository;
import dk.easv.seaticketsystem.Model.Tickets;

import java.util.List;

public class TicketService {

    private final TicketRepository ticketRepo = new TicketRepository();

    public void createTicket(Tickets ticket) {
        ticketRepo.createTicket(ticket);
    }

    public List<Tickets> getTicketsForEvent(String eventId) {
        return ticketRepo.getTicketsByEvent(eventId);
    }

    public List<Tickets> getTicketsForUser(String id) {
        return ticketRepo.getTicketsByUser(id);
    }
}