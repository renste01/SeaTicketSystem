package dk.easv.seaticketsystem.Model;

// Projekt Imports
import dk.easv.seaticketsystem.BE.TicketType;

public class TicketFormModel {

    private String customerName;
    private String customerEmail;
    private String countText;
    private String priceText;
    private TicketType ticketType;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCountText() { return countText; }
    public void setCountText(String countText) { this.countText = countText; }

    public String getPriceText() { return priceText; }
    public void setPriceText(String priceText) { this.priceText = priceText; }

    public TicketType getTicketType() { return ticketType; }
    public void setTicketType(TicketType ticketType) { this.ticketType = ticketType; }
}

