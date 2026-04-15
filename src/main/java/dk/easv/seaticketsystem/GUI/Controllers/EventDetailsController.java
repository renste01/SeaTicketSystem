package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.TicketService;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import dk.easv.seaticketsystem.BE.Event;
import dk.easv.seaticketsystem.BE.TicketType;
import dk.easv.seaticketsystem.BE.Tickets;
import dk.easv.seaticketsystem.Session.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class EventDetailsController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML private Label titleLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
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

    @FXML private ComboBox<TicketType> ticketTypeComboBox;

    private final TicketService ticketService = new TicketService();
    private Event event;

    public void setEvent(Event e) {
        this.event = e;
        loadEventDetails();
        loadTickets();
    }

    @FXML
    private void initialize() {
        colTicketId.setCellValueFactory(new PropertyValueFactory<>("ticketId"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

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
        startDateLabel.setText(event.getDate() != null ? event.getDate().format(DATE_FORMATTER) : "-");
        endDateLabel.setText(event.getEndDateTime() != null ? event.getEndDateTime().toLocalDate().format(DATE_FORMATTER) : "-");
        startTimeLabel.setText(event.getStartTime() != null ? event.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "-");
        endTimeLabel.setText(event.getEndDateTime() != null ? event.getEndDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "-");

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
                    type,
                    null
            );

            ticketService.createTicket(ticket);
            loadTickets();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Kunne ikke oprette ticket: " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleEdit() {
        EventFormController.setEventToEdit(event);
        ViewManager.getInstance().loadView("EventFormView.fxml");
    }

    @FXML
    private void handleBack() {
        ViewManager.getInstance().loadView("EventListView.fxml");
    }
}
