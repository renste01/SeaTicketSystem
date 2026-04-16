package dk.easv.seaticketsystem.GUI.Controllers;

// Project Imports
import dk.easv.seaticketsystem.BLL.EventService;
import dk.easv.seaticketsystem.BLL.TicketService;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import dk.easv.seaticketsystem.BE.Event;
import dk.easv.seaticketsystem.BE.Tickets;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

// Java Import
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketDetailsController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static Tickets selectedTicket;
    @FXML ImageView qrImageView;

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

    // NEW: barcode image
    @FXML private ImageView barcodeImageView;

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

        // Generate barcode
        generateBarcode(selectedTicket.getTicketId());

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
        qrImageView.setImage(selectedTicket.getQrCodeImage());

    }

    // Barcode generator
    private void generateBarcode(String value) {
        try {
            int width = 260;
            int height = 80;

            BitMatrix matrix = new MultiFormatWriter()
                    .encode(value, BarcodeFormat.CODE_128, width, height);

            var bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

            barcodeImageView.setImage(fxImage);

        } catch (Exception e) {
            System.err.println("Barcode generation failed: " + e.getMessage());
        }
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
                    selectedTicket.getTicketType(),
                    selectedTicket.getQrCodeText() // ← DETTE MANGLEDE
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
        return "..."; // shortened here for readability
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

