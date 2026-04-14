package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.TicketService;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.TicketType;
import dk.easv.seaticketsystem.Model.Tickets;
import dk.easv.seaticketsystem.Session.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class EventDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label locationLabel;
    @FXML private TextArea locationGuidanceArea;
    @FXML private TextArea descriptionArea;
    @FXML private Button editButton;

    @FXML private TableView<Tickets> ticketTable;
    @FXML private TableColumn<Tickets, String> colTicketId;
    @FXML private TableColumn<Tickets, Double> colPrice;

    @FXML private TextField priceField;

    // NEW: Ticket type selector
    @FXML private ComboBox<TicketType> ticketTypeComboBox;

    // FIXED: Correct service name
    private final TicketService ticketService = new TicketService();

    // FIXED: Event reference
    private Event event;

    // Called by previous controller
    public void setEvent(Event e) {
        this.event = e;
        loadEventDetails();
        loadTickets();
    }

    @FXML
    private void initialize() {

        // Setup table
        colTicketId.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Setup ticket type dropdown
        ticketTypeComboBox.getItems().setAll(
                TicketType.STANDARD,
                TicketType.FREE,
                TicketType.FREE_BEER,
                TicketType.FREE_DRINK
        );
        ticketTypeComboBox.setValue(TicketType.STANDARD);
    }

    private void loadEventDetails() {
        if (event == null) return;

        titleLabel.setText(event.getTitle());
        dateLabel.setText(event.getDate().toString());

        startTimeLabel.setText(
                event.getStartTime() != null
                        ? event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                        : "-"
        );

        endTimeLabel.setText(
                event.getEndDateTime() != null
                        ? event.getEndDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                        : "-"
        );

        locationLabel.setText(event.getLocation());
        locationGuidanceArea.setText(event.getLocationGuidance() == null ? "" : event.getLocationGuidance());
        descriptionArea.setText(event.getDescription());
    }

    private void loadTickets() {
        if (event == null) return;

        ticketTable.setItems(FXCollections.observableList(
                ticketService.getTicketsForEvent(Integer.parseInt(event.getId()))
        ));
    }

    @FXML
    private void handleCreateTicket() {
        try {
            TicketType type = ticketTypeComboBox.getValue();

            double price = (type == TicketType.FREE ||
                    type == TicketType.FREE_BEER ||
                    type == TicketType.FREE_DRINK)
                    ? 0
                    : Double.parseDouble(priceField.getText());

            Tickets ticket = new Tickets(
                    UUID.randomUUID().toString(),
                    Integer.parseInt(event.getId()),
                    null,
                    price,
                    null,
                    null,
                    "PENDING",
                    null,
                    SessionManager.getInstance().getCurrentUser().getId(),
                    type
            );

            ticketService.createTicket(ticket);
            loadTickets();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Kunne ikke oprette ticket: " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleEdit() {
        EditEventController.setEvent(event);
        ViewManager.getInstance().loadView("EditEventView.fxml");
    }

    @FXML
    private void handleBack() {
        ViewManager.getInstance().loadView("EventListView.fxml");
    }
}
