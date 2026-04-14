package dk.easv.seaticketsystem.BE;

// Java Imports
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventCoordinator extends User
{
    private final List<String> managedEventIds = new ArrayList<>();

    public EventCoordinator(String id, String firstName, String lastName, String email, String password)
    {
        super(id, firstName, lastName, email, password, UserRole.COORDINATOR);
    }

    public EventCoordinator(String firstName, String lastName, String email, String password)
    {
        this(UUID.randomUUID().toString(), firstName, lastName, email, password);
    }

    public List<String> getManagedEventIds() {return managedEventIds;}

    public void addManagedEvent(String eventId) {managedEventIds.add(eventId);}
    public void removeManagedEvent(String eventId) {managedEventIds.remove(eventId);}
}

