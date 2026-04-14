package dk.easv.seaticketsystem.Model;

// Java Imports
import java.time.LocalDate;

public class EventFormModel {

    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startTimeText;
    private String endTimeText;
    private String description;
    private String locationGuidance;
    private boolean vipEnabled;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStartTimeText() { return startTimeText; }
    public void setStartTimeText(String startTimeText) { this.startTimeText = startTimeText; }

    public String getEndTimeText() { return endTimeText; }
    public void setEndTimeText(String endTimeText) { this.endTimeText = endTimeText; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationGuidance() { return locationGuidance; }
    public void setLocationGuidance(String locationGuidance) { this.locationGuidance = locationGuidance; }

    public boolean isVipEnabled() { return vipEnabled; }
    public void setVipEnabled(boolean vipEnabled) { this.vipEnabled = vipEnabled; }
}

