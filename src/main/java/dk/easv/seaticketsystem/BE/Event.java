package dk.easv.seaticketsystem.BE;

// Java Imports
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Event {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final String id;
    private final String title;
    private final String location;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalDateTime endDateTime;
    private final String description;
    private final String locationGuidance;
    private final String ownerCoordinatorId;
    private final boolean vipEnabled;
    private final List<String> coCoordinatorIds = new ArrayList<>();

    public Event(String id, String title, String location, LocalDate date, LocalTime startTime, String description, LocalDateTime endDateTime) {
        this(id, title, location, date, startTime, description, null, endDateTime, null, false);
    }

    public Event(String id, String title, String location, LocalDate date, LocalTime startTime, String description, String ownerCoordinatorId, LocalDateTime endDateTime, String locationGuidance) {
        this(id, title, location, date, startTime, description, ownerCoordinatorId, endDateTime, locationGuidance, false);
    }

    public Event(String id, String title, String location, LocalDate date, LocalTime startTime, String description, String ownerCoordinatorId, LocalDateTime endDateTime, String locationGuidance, boolean vipEnabled) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endDateTime = endDateTime;
        this.description = description;
        this.locationGuidance = locationGuidance;
        this.ownerCoordinatorId = ownerCoordinatorId;
        this.vipEnabled = vipEnabled;
    }

    public String getId()                           { return id; }
    public String getTitle()                        { return title; }
    public String getLocation()                     { return location; }
    public LocalDate getDate()                      { return date; }
    public LocalTime getStartTime()                 { return startTime; }
    public LocalDateTime getEndDateTime()           { return endDateTime; }
    public String getDescription()                  { return description; }
    public String getLocationGuidance()             { return locationGuidance; }
    public String getOwnerCoordinatorId()           { return ownerCoordinatorId; }
    public boolean isVipEnabled()                   { return vipEnabled; }
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
        if (endDateTime == null) return date.format(DATE_FORMATTER);
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        return date.format(DATE_FORMATTER) + " → " + endDateTime.toLocalDate().format(DATE_FORMATTER) + " " + endDateTime.toLocalTime().format(timeFmt);
    }

    public String getTimeRangeDisplay() {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        String start = startTime != null ? startTime.format(timeFmt) : "-";
        String end = endDateTime != null ? endDateTime.toLocalTime().format(timeFmt) : "-";
        return start + " - " + end;
    }
}

