package dk.easv.seaticketsystem.DAL;

// Projekt Imports
import dk.easv.seaticketsystem.BE.Tickets;

// Java Imports
import java.util.List;

public interface ITicketRepository
{
    // Read
    List<Tickets> getAllTickets();
    List<Tickets> getDeletedTickets();
    List<Tickets> getTicketsByEvent(int eventId);
    List<Tickets> getTicketsByUser(String id);

    // Write
    void createTicket(Tickets tickets);
    void markTicketAsSent(String ticketId);
    void deleteTicket(String ticketId);
}


