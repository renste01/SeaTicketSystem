package dk.easv.seaticketsystem.Model;

import java.time.LocalDate;

public class Event {

    private final String id;
    private final String title;
    private final String location;
    private final LocalDate date;

    public Event(String id, String title, String location, LocalDate date) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public LocalDate getDate() { return date; }
}
