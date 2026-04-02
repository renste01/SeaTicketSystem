package dk.easv.seaticketsystem.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Event {

    private final String id;
    private final String title;
    private final String location;
    private final LocalDate date;
    private final LocalDateTime endDateTime;
    private final String description;
    private final String ownerCoordinatorId;
    private final List<String> coCoordinatorIds = new ArrayList<>();

    public Event(String id, String title, String location, LocalDate date, String description) {
        this(id, title, location, date, description, null, null);
    }

    public Event(String id, String title, String location, LocalDate date, String description, LocalDateTime endDateTime) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.endDateTime = endDateTime;
        this.description = description;
        this.ownerCoordinatorId = null;
    }

    public Event(String id, String title, String location, LocalDate date, String description, String ownerCoordinatorId) {
        this(id, title, location, date, description, ownerCoordinatorId, null);
    }

    public Event(String id, String title, String location, LocalDate date, String description, String ownerCoordinatorId, LocalDateTime endDateTime) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.endDateTime = endDateTime;
        this.description = description;
        this.ownerCoordinatorId = ownerCoordinatorId;
    }

    public String getId()                           { return id; }
    public String getTitle()                        { return title; }
    public String getLocation()                     { return location; }
    public LocalDate getDate()                      { return date; }
    public LocalDateTime getEndDateTime()           { return endDateTime; }
    public String getDescription()                  { return description; }
    public String getOwnerCoordinatorId()           { return ownerCoordinatorId; }
    public List<String> getCoCoordinatorIds()       { return coCoordinatorIds; }

    public void addCoCoordinator(String userId)     { coCoordinatorIds.add(userId); }
    public void removeCoCoordinator(String userId)  { coCoordinatorIds.remove(userId); }

    public boolean isOwnedBy(String userId) {
        return ownerCoordinatorId != null && ownerCoordinatorId.equals(userId);
    }

    public boolean hasAccess(String userId) {
        return isOwnedBy(userId) || coCoordinatorIds.contains(userId);
    }

    public String getDateRangeDisplay() {
        if (endDateTime == null) return date.toString();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        return date + " → " + endDateTime.toLocalDate() + " " + endDateTime.toLocalTime().format(timeFmt);
    }
}