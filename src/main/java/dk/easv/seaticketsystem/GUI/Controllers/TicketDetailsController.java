package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.EventService;
import dk.easv.seaticketsystem.BLL.TicketService;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.Tickets;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketDetailsController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static Tickets selectedTicket;

    @FXML private Label ticketIdLabel;
    @FXML private Label statusLabel;
    @FXML private Label priceLabel;
    @FXML private Label ticketTypeLabel;
    @FXML private Label eventTitleLabel;
    @FXML private Label eventStartDateLabel;
    @FXML private Label eventEndDateLabel;
    @FXML private Label eventTimeLabel;
    @FXML private Label eventLocationLabel;
    @FXML private Label eventDescriptionLabel;
    @FXML private Label buyerNameLabel;
    @FXML private Label buyerEmailLabel;
    @FXML private Label detailsFeedbackLabel;
    @FXML private Button sendTicketButton;

    private final EventService eventService = new EventService();
    private final TicketService ticketService = new TicketService();

    public static void setTicket(Tickets ticket) {
        selectedTicket = ticket;
    }

    @FXML
    private void initialize() {
        if (selectedTicket == null) return;

        ticketIdLabel.setText(selectedTicket.getTicketId());
        updateStatusUi();
        priceLabel.setText(selectedTicket.getPrice() + " kr");
        ticketTypeLabel.setText(selectedTicket.getTicketType().name());
        buyerNameLabel.setText(selectedTicket.getCustomerName());
        buyerEmailLabel.setText(selectedTicket.getCustomerEmail());

        Event event = findEventById(selectedTicket.getEventId());
        if (event == null) {
            eventTitleLabel.setText("Ukendt event");
            eventStartDateLabel.setText("-");
            eventEndDateLabel.setText("-");
            eventTimeLabel.setText("-");
            eventLocationLabel.setText("-");
            eventDescriptionLabel.setText("-");
            return;
        }

        eventTitleLabel.setText(event.getTitle());
        eventStartDateLabel.setText(event.getDate() != null ? event.getDate().format(DATE_FORMATTER) : "-");
        eventEndDateLabel.setText(event.getEndDateTime() != null ? event.getEndDateTime().toLocalDate().format(DATE_FORMATTER) : "-");
        eventTimeLabel.setText(event.getTimeRangeDisplay());
        eventLocationLabel.setText(event.getLocation());
        eventDescriptionLabel.setText(event.getDescription() == null ? "-" : event.getDescription());
    }

    @FXML
    private void handleBack() {
        ViewManager.getInstance().loadView("MyTicketsView.fxml");
    }

    @FXML
    private void handleDeleteTicket() {
        if (selectedTicket == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Slet billet");
        confirm.setHeaderText(null);
        confirm.setContentText("Er du sikker på, at du vil slette denne billet?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        ticketService.deleteTicket(selectedTicket.getTicketId());
        ViewManager.getInstance().loadView("MyTicketsView.fxml");
    }

    @FXML
    private void handleSendTicket() {
        if (selectedTicket == null) return;
        if ("SENT".equalsIgnoreCase(selectedTicket.getDeliveryStatus())) {
            showFeedback("Billet er allerede sendt.", false);
            return;
        }

        try {
            ticketService.markTicketAsSent(selectedTicket.getTicketId());
            selectedTicket = new Tickets(
                    selectedTicket.getTicketId(),
                    selectedTicket.getEventId(),
                    selectedTicket.getUserID(),
                    selectedTicket.getPrice(),
                    selectedTicket.getCustomerName(),
                    selectedTicket.getCustomerEmail(),
                    "SENT",
                    java.time.LocalDateTime.now(),
                    selectedTicket.getIssuedByCoordinatorId(),
                    selectedTicket.getTicketType()
            );
            updateStatusUi();
            showFeedback("Billet markeret som sendt.", true);
        } catch (Exception e) {
            showFeedback("Kunne ikke sende billet: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handlePrintTicket() {
        if (selectedTicket == null) return;

        Event event = findEventById(selectedTicket.getEventId());

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Gem billet som PDF");
        fileChooser.setInitialFileName("billet-" + selectedTicket.getTicketId() + ".html");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML fil (åbn i browser og print som PDF)", "*.html")
        );

        Stage stage = (Stage) ticketIdLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(generateTicketHtml(selectedTicket, event));
            showFeedback("Billet gemt! Åbn filen i din browser og tryk Ctrl+P for at printe som PDF.", true);
        } catch (Exception e) {
            showFeedback("Kunne ikke gemme billet: " + e.getMessage(), false);
        }
    }

    private String generateTicketHtml(Tickets ticket, Event event) {
        String eventTitle = event != null ? event.getTitle() : "Ukendt event";
        String eventDate = event != null && event.getDate() != null ? event.getDate().format(DATE_FORMATTER) : "-";
        String eventLocation = event != null ? event.getLocation() : "-";
        String eventTime = event != null ? event.getTimeRangeDisplay() : "-";

        return """
            <!DOCTYPE html>
            <html lang="da">
            <head>
                <meta charset="UTF-8">
                <title>Billet - %s</title>
                <style>
                    body { font-family: 'Segoe UI', sans-serif; background: #f4f7f8; margin: 0; padding: 40px; }
                    .ticket { background: white; border-radius: 16px; padding: 40px; max-width: 600px;
                               margin: 0 auto; box-shadow: 0 4px 20px rgba(0,0,0,0.10); }
                    .header { background: linear-gradient(to right, #002430, #004a5a);
                               border-radius: 10px; padding: 24px; color: white; margin-bottom: 28px; }
                    .header h1 { margin: 0 0 4px 0; font-size: 22px; }
                    .header p { margin: 0; opacity: 0.7; font-size: 13px; }
                    .badge { display: inline-block; background: #db3629; color: white;
                              font-size: 11px; font-weight: bold; padding: 3px 12px;
                              border-radius: 20px; margin-top: 8px; }
                    .section { margin-bottom: 20px; }
                    .section-title { font-size: 11px; font-weight: bold; color: #888;
                                      letter-spacing: 1.5px; margin-bottom: 10px; }
                    .row { display: flex; justify-content: space-between;
                            border-bottom: 1px solid #f0f0f0; padding: 8px 0; }
                    .row:last-child { border-bottom: none; }
                    .key { color: #888; font-size: 13px; }
                    .value { color: #002430; font-size: 13px; font-weight: bold; }
                    .ticket-id { font-family: 'Courier New', monospace; font-size: 16px;
                                  font-weight: bold; color: #002430; letter-spacing: 2px; }
                    .footer { text-align: center; color: #aaa; font-size: 11px; margin-top: 28px; }
                </style>
            </head>
            <body>
                <div class="ticket">
                    <div class="header">
                        <h1>SEA Billetsystem</h1>
                        <p>Din officielle billet</p>
                        <span class="badge">%s</span>
                    </div>

                    <div class="section">
                        <div class="section-title">BILLET INFO</div>
                        <div class="row">
                            <span class="key">Ticket ID</span>
                            <span class="ticket-id">%s</span>
                        </div>
                        <div class="row">
                            <span class="key">Køber</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="row">
                            <span class="key">E-mail</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="row">
                            <span class="key">Billet type</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="row">
                            <span class="key">Pris</span>
                            <span class="value">%s kr</span>
                        </div>
                    </div>

                    <div class="section">
                        <div class="section-title">EVENT INFO</div>
                        <div class="row">
                            <span class="key">Event</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="row">
                            <span class="key">Dato</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="row">
                            <span class="key">Tid</span>
                            <span class="value">%s</span>
                        </div>
                        <div class="row">
                            <span class="key">Lokation</span>
                            <span class="value">%s</span>
                        </div>
                    </div>

                    <div class="footer">
                        Tak for dit køb &mdash; SEA Billetsystem
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                eventTitle,
                ticket.getTicketType().name(),
                ticket.getTicketId(),
                ticket.getCustomerName(),
                ticket.getCustomerEmail(),
                ticket.getTicketType().name(),
                ticket.getPrice(),
                eventTitle,
                eventDate,
                eventTime,
                eventLocation
        );
    }

    private Event findEventById(int eventId) {
        List<Event> allEvents = eventService.getAllEvents();
        for (Event event : allEvents) {
            try {
                if (Integer.parseInt(event.getId()) == eventId) return event;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void updateStatusUi() {
        String status = selectedTicket.getDeliveryStatus();
        statusLabel.setText(status);
        statusLabel.getStyleClass().removeAll("status-pending", "status-sent");
        if ("SENT".equalsIgnoreCase(status)) {
            statusLabel.getStyleClass().add("status-sent");
            sendTicketButton.setDisable(true);
        } else {
            statusLabel.getStyleClass().add("status-pending");
            sendTicketButton.setDisable(false);
        }
    }

    private void showFeedback(String message, boolean success) {
        detailsFeedbackLabel.setText(message);
        detailsFeedbackLabel.getStyleClass().removeAll("feedback-success", "feedback-error");
        detailsFeedbackLabel.getStyleClass().add(success ? "feedback-success" : "feedback-error");
        detailsFeedbackLabel.setVisible(true);
        detailsFeedbackLabel.setManaged(true);
    }
}