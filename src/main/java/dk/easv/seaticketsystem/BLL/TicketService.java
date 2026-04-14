package dk.easv.seaticketsystem.BLL;

// Projekt Imports
import dk.easv.seaticketsystem.DAL.ITicketRepository;
import dk.easv.seaticketsystem.DAL.TicketRepository;
import dk.easv.seaticketsystem.BE.Tickets;

// Java Imports
import java.util.List;

public class TicketService {

    private final ITicketRepository ticketRepository;

    public TicketService() {
        this.ticketRepository = new TicketRepository();
    }

    public void createTicket(Tickets ticket) {
        ticketRepository.createTicket(ticket);
    }

    public List<Tickets> getTicketsForEvent(int eventId) {
        return ticketRepository.getTicketsByEvent(eventId);
    }

    public List<Tickets> getTicketsForUser(String id) {
        return ticketRepository.getTicketsByUser(id);
    }

    public List<Tickets> getAllTickets() {
        return ticketRepository.getAllTickets();
    }

    public void markTicketAsSent(String ticketId) {
        ticketRepository.markTicketAsSent(ticketId);
    }

    public void deleteTicket(String ticketId) {
        ticketRepository.deleteTicket(ticketId);
    }
}

