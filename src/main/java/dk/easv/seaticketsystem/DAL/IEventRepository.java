package dk.easv.seaticketsystem.DAL;

// Projekt Imports
import dk.easv.seaticketsystem.Model.Event;

// Java Imports
import java.util.List;

public interface IEventRepository
{
    // Read
    List<Event> getAllEvents();

    // Write
    Event createEvent(Event event);
    void updateEvent(Event event);
    void deleteEvent(String eventId);
}

