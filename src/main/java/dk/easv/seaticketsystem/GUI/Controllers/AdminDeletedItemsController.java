package dk.easv.seaticketsystem.GUI.Controllers;

// Projekt Imports
import dk.easv.seaticketsystem.BE.Event;
import dk.easv.seaticketsystem.BE.Tickets;
import dk.easv.seaticketsystem.BE.User;
import dk.easv.seaticketsystem.BLL.EventService;
import dk.easv.seaticketsystem.BLL.TicketService;
import dk.easv.seaticketsystem.BLL.UserService;

// Java Imports
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AdminDeletedItemsController implements Initializable {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML private TableView<User> deletedUsersTable;
    @FXML private TableColumn<User, String> colDeletedUserName;
    @FXML private TableColumn<User, String> colDeletedUserEmail;
    @FXML private TableColumn<User, String> colDeletedUserRole;

    @FXML private TableView<Event> deletedEventsTable;
    @FXML private TableColumn<Event, String> colDeletedEventTitle;
    @FXML private TableColumn<Event, String> colDeletedEventDate;
    @FXML private TableColumn<Event, String> colDeletedEventLocation;

    @FXML private TableView<Tickets> deletedTicketsTable;
    @FXML private TableColumn<Tickets, String> colDeletedTicketId;
    @FXML private TableColumn<Tickets, String> colDeletedTicketEventId;
    @FXML private TableColumn<Tickets, String> colDeletedTicketCustomer;
    @FXML private TableColumn<Tickets, String> colDeletedTicketStatus;

    private final UserService userService = new UserService();
    private final EventService eventService = new EventService();
    private final TicketService ticketService = new TicketService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupDeletedUserColumns();
        setupDeletedEventColumns();
        setupDeletedTicketColumns();
        loadDeletedItems();
    }

    private void setupDeletedUserColumns() {
        colDeletedUserName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colDeletedUserEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        colDeletedUserRole.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getRole().getDisplayName()));
    }

    private void setupDeletedEventColumns() {
        colDeletedEventTitle.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        colDeletedEventDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate().format(DATE_FORMATTER)));
        colDeletedEventLocation.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLocation()));
    }

    private void setupDeletedTicketColumns() {
        colDeletedTicketId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTicketId()));
        colDeletedTicketEventId.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getEventId())));
        colDeletedTicketCustomer.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCustomerName()));
        colDeletedTicketStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDeliveryStatus()));
    }

    private void loadDeletedItems() {
        deletedUsersTable.getItems().setAll(userService.getDeletedUsers());
        deletedEventsTable.getItems().setAll(eventService.getDeletedEvents());
        deletedTicketsTable.getItems().setAll(ticketService.getDeletedTickets());
    }
}

