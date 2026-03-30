package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.Tickets;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import dk.easv.seaticketsystem.BLL.TicketService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.UUID;

public class EventDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label locationLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Button editButton;

    // Ticket GUI
    @FXML private TableView<Tickets> ticketTable;
    @FXML private TableColumn<Tickets, String> colTicketId;
    @FXML private TableColumn<Tickets, Double> colPrice;
    @FXML private TextField priceField;

    private final TicketService ticketService = new TicketService();

    private static Event selectedEvent;

    public static void setEvent(Event e) {
        selectedEvent = e;
    }

    @FXML
    private void initialize() {

        // Load event details
        if (selectedEvent != null) {
            titleLabel.setText(selectedEvent.getTitle());
            dateLabel.setText(selectedEvent.getDate().toString());
            locationLabel.setText(selectedEvent.getLocation());
            descriptionArea.setText(selectedEvent.getDescription());
        }

        // Hide edit button unless coordinator
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null || user.getRole() != UserRole.COORDINATOR) {
            editButton.setVisible(false);
            editButton.setManaged(false);
        }

        // Setup ticket table columns
        colTicketId.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Load tickets for this event
        loadTickets();
    }

    private void loadTickets() {
        if (selectedEvent == null) return;

        ticketTable.setItems(FXCollections.observableList(
                ticketService.getTicketsForEvent(selectedEvent.getId())
        ));
    }

    @FXML
    private void handleCreateTicket() {
        try {
            String ticketId = UUID.randomUUID().toString();
            String eventId = selectedEvent.getId();
            String userId = null; // optional
            double price = Double.parseDouble(priceField.getText());

            Tickets ticket = new Tickets(ticketId, eventId, userId, price);
            ticketService.createTicket(ticket);

            priceField.clear();
            loadTickets();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Kunne ikke oprette ticket: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleEdit() {
        EditEventController.setEvent(selectedEvent);
        ViewManager.getInstance().loadView("EditEventView.fxml");
    }

    @FXML
    private void handleBack() {
        ViewManager.getInstance().loadView("EventListView.fxml");
    }
}