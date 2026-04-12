package dk.easv.seaticketsystem.BLL;

// Projekt Imports
import dk.easv.seaticketsystem.DAL.DBConnector;
import dk.easv.seaticketsystem.DAL.IEventRepository;
import dk.easv.seaticketsystem.DAL.EventRepository;
import dk.easv.seaticketsystem.Model.Event;

// Java Imports
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class EventService {

    private final IEventRepository eventRepository;

    public EventService() {
        this.eventRepository = new EventRepository();
    }

    public EventService(IEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    private static final List<Event> offlineEvents = new ArrayList<>(List.of(
            new Event("1", "Koncert i Havnen", "Esbjerg Havn", java.time.LocalDate.of(2025, 6, 12), java.time.LocalTime.of(18, 0), "Live concert at the harbor", java.time.LocalDateTime.of(2025, 6, 12, 22, 0)),
            new Event("2", "Sommerfestival", "Musikhuset", java.time.LocalDate.of(2025, 7, 3), java.time.LocalTime.of(19, 0), "Summer festival with music and food", java.time.LocalDateTime.of(2025, 7, 3, 23, 30))
    ));

    private boolean databaseAvailable() {
        try (Connection c = DBConnector.getInstance().getConnection()) {
            return c != null && !c.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public List<Event> getAllEvents() {
        if (!databaseAvailable()) {
            return new ArrayList<>(offlineEvents);
        }
        return eventRepository.getAllEvents();
    }

    public Event createEvent(Event event) {
        if (!databaseAvailable()) {
            String nextId = String.valueOf(offlineEvents.size() + 1);
            Event created = new Event(
                    nextId,
                    event.getTitle(),
                    event.getLocation(),
                    event.getDate(),
                    event.getStartTime(),
                    event.getDescription(),
                    event.getOwnerCoordinatorId(),
                    event.getEndDateTime(),
                    event.getLocationGuidance(),
                    event.isVipEnabled()
            );
            event.getCoCoordinatorIds().forEach(created::addCoCoordinator);
            offlineEvents.add(created);
            return created;
        }
        return eventRepository.createEvent(event);
    }

    public void updateEvent(Event event) {
        if (!databaseAvailable()) {
            for (int i = 0; i < offlineEvents.size(); i++) {
                Event current = offlineEvents.get(i);
                if (current.getId().equals(event.getId())) {
                    offlineEvents.set(i, event);
                    return;
                }
            }
            return;
        }
        eventRepository.updateEvent(event);
    }

    public void deleteEvent(String eventId) {
        if (!databaseAvailable()) {
            offlineEvents.removeIf(e -> e.getId().equals(eventId));
            return;
        }
        eventRepository.deleteEvent(eventId);
    }
}
