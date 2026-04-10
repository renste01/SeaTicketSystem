package dk.easv.seaticketsystem.DAL;

import dk.easv.seaticketsystem.Model.Tickets;
import dk.easv.seaticketsystem.Model.TicketType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketRepository {

    public void createTicket (Tickets tickets){
        String sql = "INSERT INTO Tickets (TicketId, EventID, UserId, Price, CustomerName, CustomerEmail, DeliveryStatus, SentAt, IssuedByCoordinatorId, TicketType) VALUES(?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tickets.getTicketId());
            stmt.setInt(2, tickets.getEventId());
            stmt.setString(3, tickets.getUserID());
            stmt.setString(4, String.valueOf(tickets.getPrice()));
            stmt.setString(5, tickets.getCustomerName());
            stmt.setString(6, tickets.getCustomerEmail());
            stmt.setString(7, tickets.getDeliveryStatus());
            if (tickets.getSentAt() == null) {
                stmt.setTimestamp(8, null);
            } else {
                stmt.setTimestamp(8, Timestamp.valueOf(tickets.getSentAt()));
            }
            stmt.setString(9, tickets.getIssuedByCoordinatorId());
            stmt.setString(10, tickets.getTicketType().name());

            stmt.executeUpdate();

        } catch (Exception e){
            throw new RuntimeException("Kunne ikke oprette ticket i databasen: " + e.getMessage(), e);
        }

    }
    public List<Tickets> getTicketsByEvent(int eventId) {
        List<Tickets> tickets = new ArrayList<>();

        String sql = "SELECT * FROM Tickets WHERE EventId = ? ORDER BY TicketId DESC";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tickets.add(mapTicket(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke hente tickets", e);
        }

        return tickets;
    }



    public List<Tickets> getTicketsByUser(String id) {
        List<Tickets> tickets = new ArrayList<>();

        String sql = "SELECT * FROM Tickets WHERE UserId = ? ORDER BY TicketId DESC";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tickets.add(mapTicket(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke hente brugerens tickets", e);
        }

        return tickets;
    }

    public List<Tickets> getAllTickets() {
        List<Tickets> tickets = new ArrayList<>();
        String sql = "SELECT * FROM Tickets ORDER BY TicketId DESC";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tickets.add(mapTicket(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke hente tickets", e);
        }

        return tickets;
    }

    public void markTicketAsSent(String ticketId) {
        String sql = "UPDATE Tickets SET DeliveryStatus = 'SENT', SentAt = ? WHERE TicketId = ?";
        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, ticketId);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke opdatere ticket-status", e);
        }
    }

    public void deleteTicket(String ticketId) {
        String sql = "DELETE FROM Tickets WHERE TicketId = ?";
        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ticketId);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke slette ticket", e);
        }
    }

    private Tickets mapTicket(ResultSet rs) throws Exception {
        Timestamp sentAtValue = null;
        try {
            sentAtValue = rs.getTimestamp("SentAt");
        } catch (Exception ignored) {}

        String customerName = null;
        String customerEmail = null;
        String deliveryStatus = null;
        String issuedBy = null;
        String ticketType = "STANDARD";

        try { customerName = rs.getString("CustomerName"); } catch (Exception ignored) {}
        try { customerEmail = rs.getString("CustomerEmail"); } catch (Exception ignored) {}
        try { deliveryStatus = rs.getString("DeliveryStatus"); } catch (Exception ignored) {}
        try { issuedBy = rs.getString("IssuedByCoordinatorId"); } catch (Exception ignored) {}
        try { ticketType = rs.getString("TicketType"); } catch (Exception ignored) {}

        String priceValue = rs.getString("Price");
        double parsedPrice = priceValue == null || priceValue.isBlank() ? 0.0 : Double.parseDouble(priceValue);

        TicketType parsedTicketType;
        try {
            parsedTicketType = TicketType.valueOf(ticketType == null ? "STANDARD" : ticketType.toUpperCase());
        } catch (Exception e) {
            parsedTicketType = TicketType.STANDARD;
        }

        return new Tickets(
                rs.getString("TicketId"),
                rs.getInt("EventId"),
                rs.getString("UserId"),
                parsedPrice,
                customerName,
                customerEmail,
                deliveryStatus,
                sentAtValue == null ? null : sentAtValue.toLocalDateTime(),
                issuedBy,
                parsedTicketType
        );
    }
}
