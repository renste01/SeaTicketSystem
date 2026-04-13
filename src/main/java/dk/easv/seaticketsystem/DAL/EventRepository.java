package dk.easv.seaticketsystem.DAL;

// Projekt Imports
import dk.easv.seaticketsystem.Model.Event;

// Java Imports
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EventRepository implements IEventRepository {

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT EventId, Title, Location, StartDate, EndDate, StartTime, EndTime, Description, OwnerCoordinatorId, LocationGuidance, VipEnabled FROM Events";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                events.add(mapEvent(rs));
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Kunne ikke hente events", e);
        }

        return events;
    }

    public Event createEvent(Event event) {
        String sql = "INSERT INTO Events (Title, Location, StartDate, EndDate, StartTime, EndTime, Description, OwnerCoordinatorId, LocationGuidance, VipEnabled) VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getLocation());
            stmt.setString(3, event.getDate().toString());
            stmt.setString(4, event.getEndDateTime() != null ? event.getEndDateTime().toLocalDate().toString() : null);
            stmt.setString(5, event.getStartTime() != null ? event.getStartTime().toString() : null);
            stmt.setString(6, event.getEndDateTime() != null ? event.getEndDateTime().toLocalTime().toString() : null);
            stmt.setString(7, event.getDescription());
            stmt.setString(8, event.getOwnerCoordinatorId());
            stmt.setString(9, event.getLocationGuidance());
            stmt.setBoolean(10, event.isVipEnabled());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    String newId = String.valueOf(keys.getInt(1));
                    Event created = new Event(
                            newId,
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
                    return created;
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Kunne ikke oprette event", e);
        }

        throw new RuntimeException("Event blev oprettet uden returneret EventId");
    }

    public void updateEvent(Event event) {
        String sql = "UPDATE Events SET Title = ?, Location = ?, StartDate = ?, EndDate = ?, StartTime = ?, EndTime = ?, Description = ?, OwnerCoordinatorId = ?, LocationGuidance = ?, VipEnabled = ? WHERE EventId = ?";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getLocation());
            stmt.setString(3, event.getDate().toString());
            stmt.setString(4, event.getEndDateTime() != null ? event.getEndDateTime().toLocalDate().toString() : null);
            stmt.setString(5, event.getStartTime() != null ? event.getStartTime().toString() : null);
            stmt.setString(6, event.getEndDateTime() != null ? event.getEndDateTime().toLocalTime().toString() : null);
            stmt.setString(7, event.getDescription());
            stmt.setString(8, event.getOwnerCoordinatorId());
            stmt.setString(9, event.getLocationGuidance());
            stmt.setBoolean(10, event.isVipEnabled());
            stmt.setInt(11, Integer.parseInt(event.getId()));
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Kunne ikke opdatere event", e);
        }
    }

    public void deleteEvent(String eventId) {
        String sql = "DELETE FROM Events WHERE EventId = ?";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(eventId));
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Kunne ikke slette event", e);
        }
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        String id = String.valueOf(rs.getInt("EventId"));
        String title = rs.getString("Title");
        String location = rs.getString("Location");
        LocalDate startDate = LocalDate.parse(rs.getString("StartDate"));
        String endDateText = rs.getString("EndDate");
        LocalDate endDate = endDateText == null || endDateText.isBlank() ? startDate : LocalDate.parse(endDateText);
        LocalTime startTime = LocalTime.parse(rs.getString("StartTime"));
        LocalTime endTime = LocalTime.parse(rs.getString("EndTime"));
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
        String description = rs.getString("Description");
        String ownerCoordinatorId = rs.getString("OwnerCoordinatorId");
        String locationGuidance = rs.getString("LocationGuidance");
        boolean vipEnabled = rs.getBoolean("VipEnabled");

        return new Event(id, title, location, startDate, startTime, description, ownerCoordinatorId, endDateTime, locationGuidance, vipEnabled);
    }
}
