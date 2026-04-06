package dk.easv.seaticketsystem.DAL;

import dk.easv.seaticketsystem.Model.Tickets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TicketRepository {

    public void createTicket (Tickets tickets){
        String sql = "INSERT INTO Tickets (TicketId, EventID, UserId, Price) VALUES(?,?,?,?)";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tickets.getTicketId());
            stmt.setInt(2, tickets.getEventId());
            stmt.setString(3, tickets.getUserID());
            stmt.setDouble(4,tickets.getPrice());

            stmt.executeUpdate();

        } catch (Exception e){
            throw new RuntimeException("kunne ikke opretter forbindelse :(", e);
        }

    }
    public List<Tickets> getTicketsByEvent(int eventId) {
        List<Tickets> tickets = new ArrayList<>();

        String sql = "SELECT * FROM Tickets WHERE EventId = ?";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tickets.add(new Tickets(
                        rs.getString("TicketId"),
                        rs.getInt("EventId"),
                        rs.getString("UserId"),
                        rs.getDouble("Price")
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke hente tickets", e);
        }

        return tickets;
    }



    public List<Tickets> getTicketsByUser(String id) {
        List<Tickets> tickets = new ArrayList<>();

        String sql = "SELECT * FROM Tickets WHERE UserId = ?";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tickets.add(new Tickets(
                        rs.getString("TicketId"),
                        rs.getInt("EventId"),
                        rs.getString("UserId"),
                        rs.getDouble("Price")
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke hente brugerens tickets", e);
        }

        return tickets;
    }
}
