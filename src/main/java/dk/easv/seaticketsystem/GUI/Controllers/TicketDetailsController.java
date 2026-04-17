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
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Apache PDFBox imports
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketDetailsController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static Tickets selectedTicket;

    @FXML private ImageView qrImageView;
    @FXML private ImageView barcodeImageView;

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

    private void generateBarcode(String value) {
        try {
            int width = 260;
            int height = 80;

            BitMatrix matrix = new MultiFormatWriter()
                    .encode(value, BarcodeFormat.CODE_128, width, height);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
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
                    LocalDateTime.now(),
                    selectedTicket.getIssuedByCoordinatorId(),
                    selectedTicket.getTicketType(),
                    selectedTicket.getQrCodeText()
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
        fileChooser.setInitialFileName("billet-" + selectedTicket.getTicketId() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF fil", "*.pdf")
        );

        Stage stage = (Stage) ticketIdLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try {
            createRealPdf(file, selectedTicket, event);
            showFeedback("PDF gemt: " + file.getName(), true);
        } catch (Exception e) {
            showFeedback("Kunne ikke gemme PDF: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    private void createRealPdf(File file, Tickets ticket, Event event) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float yPosition = yStart;
                float lineHeight = 20;

                // Header
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("SEA BILLETSYSTEM");
                contentStream.endText();
                yPosition -= lineHeight * 1.5f;

                // Line separator
                contentStream.setStrokingColor(200, 200, 200);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= lineHeight;

                // Ticket section
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.setNonStrokingColor(219, 54, 41);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Billet detaljer");
                contentStream.endText();
                yPosition -= lineHeight;

                // Ticket details
                contentStream.setNonStrokingColor(0, 0, 0);

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Billet ID:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(margin + 100, yPosition);
                contentStream.showText(ticket.getTicketId());
                contentStream.endText();
                yPosition -= lineHeight;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Pris:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(margin + 100, yPosition);
                contentStream.showText(String.format("%.2f kr", ticket.getPrice()));
                contentStream.endText();
                yPosition -= lineHeight;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Type:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(margin + 100, yPosition);
                contentStream.showText(ticket.getTicketType().name());
                contentStream.endText();
                yPosition -= lineHeight;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Status:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(margin + 100, yPosition);
                contentStream.showText(ticket.getDeliveryStatus());
                contentStream.endText();
                yPosition -= lineHeight;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Køber:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(margin + 100, yPosition);
                contentStream.showText(ticket.getCustomerName());
                contentStream.endText();
                yPosition -= lineHeight;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("E-mail:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.newLineAtOffset(margin + 100, yPosition);
                contentStream.showText(ticket.getCustomerEmail());
                contentStream.endText();
                yPosition -= lineHeight * 1.5f;

                // Event section
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.setNonStrokingColor(219, 54, 41);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Event information");
                contentStream.endText();
                yPosition -= lineHeight;

                if (event != null) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Event titel:");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    contentStream.newLineAtOffset(margin + 100, yPosition);
                    contentStream.showText(event.getTitle());
                    contentStream.endText();
                    yPosition -= lineHeight;

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Dato:");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    contentStream.newLineAtOffset(margin + 100, yPosition);
                    contentStream.showText(event.getDate() != null ? event.getDate().format(DATE_FORMATTER) : "-");
                    contentStream.endText();
                    yPosition -= lineHeight;

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Tid:");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    contentStream.newLineAtOffset(margin + 100, yPosition);
                    contentStream.showText(event.getTimeRangeDisplay());
                    contentStream.endText();
                    yPosition -= lineHeight;

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Sted:");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    contentStream.newLineAtOffset(margin + 100, yPosition);
                    contentStream.showText(event.getLocation());
                    contentStream.endText();
                    yPosition -= lineHeight;
                }

                yPosition -= lineHeight;

                // Add barcode image
                if (barcodeImageView.getImage() != null) {
                    BufferedImage barcodeImage = convertToBufferedImage(barcodeImageView.getImage());
                    if (barcodeImage != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(barcodeImage, "PNG", baos);
                        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, baos.toByteArray(), "barcode");
                        contentStream.drawImage(pdImage, margin, yPosition - 80, 260, 80);
                        yPosition -= 90;
                    }
                }

                // Add QR code image
                if (qrImageView.getImage() != null) {
                    BufferedImage qrImage = convertToBufferedImage(qrImageView.getImage());
                    if (qrImage != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(qrImage, "PNG", baos);
                        PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, baos.toByteArray(), "qrcode");
                        contentStream.drawImage(pdImage, margin + 50, yPosition - 150, 150, 150);
                        yPosition -= 160;
                    }
                }

                // Footer
                yPosition -= lineHeight * 2;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                contentStream.setNonStrokingColor(100, 100, 100);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Gyldig ved fremvisning ved indgangen. Billetten er personlig og kan ikke overdrages.");
                contentStream.endText();
                yPosition -= lineHeight;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("SEA Ticket System - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
                contentStream.endText();
            }

            document.save(file);
        }
    }

    private BufferedImage convertToBufferedImage(Image fxImage) {
        if (fxImage == null) return null;
        try {
            int width = (int) fxImage.getWidth();
            int height = (int) fxImage.getHeight();
            WritableImage writableImage = new WritableImage(width, height);
            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(width, height);
            canvas.getGraphicsContext2D().drawImage(fxImage, 0, 0);
            javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
            return SwingFXUtils.fromFXImage(canvas.snapshot(params, null), null);
        } catch (Exception e) {
            return null;
        }
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