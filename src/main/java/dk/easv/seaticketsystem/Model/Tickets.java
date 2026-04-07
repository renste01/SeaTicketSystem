package dk.easv.seaticketsystem.Model;

public class Tickets {

    private final String ticketId;
    private final int eventId;
    private final String userID;
    private final double price;


    public Tickets(String ticketId, int eventId, String userID, double price ) {
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.userID = userID;
        this.price = price;

    }
        public String getTicketId() {
            return ticketId;
        }

        public int getEventId() {
            return eventId;
        }

        public String getUserID() {
            return userID;
        }

        public double getPrice() {
            return price;
        }
    }

