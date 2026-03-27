package dk.easv.seaticketsystem.Model;

import java.time.LocalDate;

public class Event {

    private final String id;
    private final String title;
    private final String location;
    private final LocalDate date;
    private final String description;

    public Event(String id, String title, String location, LocalDate date, String description) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
