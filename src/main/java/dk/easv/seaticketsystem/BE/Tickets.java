package dk.easv.seaticketsystem.BE;

import dk.easv.seaticketsystem.GUI.Util.QRGenerator;
import javafx.scene.image.Image;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Tickets {

    private final String ticketId;
    private final int eventId;
    private final String userID;
    private final double price;
    private final String customerName;
    private final String customerEmail;
    private final String deliveryStatus;
    private final LocalDateTime sentAt;
    private final String issuedByCoordinatorId;
    private final TicketType ticketType;

    private final String qrCodeText;

    public Tickets(String ticketId, int eventId, String userID, double price) {
        this(ticketId, eventId, userID, price,
                null, null, "PENDING", null, null,
                TicketType.STANDARD, null);
    }

    public Tickets(String ticketId, int eventId, String userID, double price,
                   String customerName, String customerEmail, String deliveryStatus,
                   LocalDateTime sentAt, String issuedByCoordinatorId) {
        this(ticketId, eventId, userID, price,
                customerName, customerEmail, deliveryStatus, sentAt,
                issuedByCoordinatorId, TicketType.STANDARD, null);
    }

    public Tickets(String ticketId, int eventId, String userID, double price,
                   String customerName, String customerEmail, String deliveryStatus,
                   LocalDateTime sentAt, String issuedByCoordinatorId,
                   TicketType ticketType, String qrCodeTextFromDB) {

        this.ticketId = ticketId;
        this.eventId = eventId;
        this.userID = userID;
        this.price = price;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.deliveryStatus = deliveryStatus == null ? "PENDING" : deliveryStatus;
        this.sentAt = sentAt;
        this.issuedByCoordinatorId = issuedByCoordinatorId;
        this.ticketType = ticketType == null ? TicketType.STANDARD : ticketType;

        this.qrCodeText = (qrCodeTextFromDB == null || qrCodeTextFromDB.isBlank())
                ? generateQrText()
                : qrCodeTextFromDB;
    }

    private String generateQrText() {
        return ticketId + "-" + UUID.randomUUID();
    }

    public String getQrCodeText() {
        return qrCodeText;
    }

    public Image getQrCodeImage() {
        return QRGenerator.generateQRCode(qrCodeText, 250);
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

    public String getCustomerName() {
        return customerName == null ? "" : customerName;
    }

    public String getCustomerEmail() {
        return customerEmail == null ? "" : customerEmail;
    }

    public String getDeliveryStatus() {
        return deliveryStatus == null ? "PENDING" : deliveryStatus;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getSentAtDisplay() {
        if (sentAt == null) return "-";
        return sentAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    public String getIssuedByCoordinatorId() {
        return issuedByCoordinatorId;
    }

    public TicketType getTicketType() {
        return ticketType;
    }
}
