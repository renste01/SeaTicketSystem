package dk.easv.seaticketsystem.GUI.Controllers;

// Project Imports
import dk.easv.seaticketsystem.BLL.EventService;
import dk.easv.seaticketsystem.BLL.TicketService;
import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.BE.Event;
import dk.easv.seaticketsystem.BE.Tickets;
import dk.easv.seaticketsystem.BE.TicketType;
import dk.easv.seaticketsystem.BE.User;
import dk.easv.seaticketsystem.BE.UserRole;
import dk.easv.seaticketsystem.Model.TicketFormModel;
import dk.easv.seaticketsystem.Session.SessionManager;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;

// Java Imports
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MyTicketsController {

    private boolean showOnlyFreeTickets = false;
    public void setShowOnlyFreeTickets(boolean value) {
        this.showOnlyFreeTickets = value;
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML private ComboBox<Event> eventComboBox;
    @FXML private ComboBox<User> receiverUserComboBox;
    @FXML private TextField customerNameField;
    @FXML private TextField customerEmailField;
    @FXML private ComboBox<TicketType> ticketTypeComboBox;
    @FXML private TextField ticketCountField;
    @FXML private TextField priceField;
    @FXML private Label ticketFeedbackLabel;
    @FXML private TableView<Tickets> ticketTable;
    @FXML private TableColumn<Tickets, String> colEvent;
    @FXML private TableColumn<Tickets, String> colCustomer;
    @FXML private TableColumn<Tickets, String> colEmail;
    @FXML private TableColumn<Tickets, String> colType;
    @FXML private TableColumn<Tickets, Double> colPrice;
    @FXML private TableColumn<Tickets, String> colStatus;
    @FXML private TableColumn<Tickets, String> colSentAt;

    private final TicketService ticketService = new TicketService();
    private final EventService eventService = new EventService();
    private final UserService userService = new UserService();
    private User currentUser;
    private final Map<Integer, String> eventNameById = new HashMap<>();

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        colEvent.setCellValueFactory(c -> {
            int eventId = c.getValue().getEventId();
            String eventName = eventNameById.get(eventId);
            return new SimpleStringProperty(eventName == null ? ("Event #" + eventId) : eventName);
        });
        colCustomer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerEmail()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTicketType().name()));
        colPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrice()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeliveryStatus()));
        colSentAt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSentAtDisplay()));

        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                if (item % 1 == 0) {
                    setText(String.format("%.0f kr", item));
                } else {
                    setText(String.format("%.2f kr", item).replace('.', ','));
                }
            }
        });

        eventComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                String formattedDate = (item == null || item.getDate() == null) ? "-" : item.getDate().format(DATE_FORMATTER);
                setText(empty || item == null ? null : item.getTitle() + " (" + formattedDate + ")");
            }
        });
        eventComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                String formattedDate = (item == null || item.getDate() == null) ? "-" : item.getDate().format(DATE_FORMATTER);
                setText(empty || item == null ? null : item.getTitle() + " (" + formattedDate + ")");
            }
        });

        eventComboBox.valueProperty().addListener((obs, oldEvent, newEvent) -> updateTicketTypeChoices(newEvent));

        receiverUserComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getEmail() + ")");
            }
        });
        receiverUserComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getEmail() + ")");
            }
        });

        receiverUserComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                customerNameField.setText(newValue.getName());
                customerEmailField.setText(newValue.getEmail());
            }
        });

        ticketTypeComboBox.getItems().setAll(
                TicketType.STANDARD,
                TicketType.FREE,
                TicketType.FREE_BEER,
                TicketType.FREE_DRINK
        );
        ticketTypeComboBox.setValue(TicketType.STANDARD);

        ticketTable.setRowFactory(tv -> {
            TableRow<Tickets> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openTicketDetails(row.getItem());
                }
            });
            return row;
        });

        loadEvents();
        loadReceiverUsers();
        loadTickets();
    }

    @FXML
    private void handleIssueTickets() {
        Event selectedEvent = eventComboBox.getValue();
        TicketFormModel form = new TicketFormModel();
        form.setCustomerName(customerNameField.getText() == null ? "" : customerNameField.getText().trim());
        form.setCustomerEmail(customerEmailField.getText() == null ? "" : customerEmailField.getText().trim());
        form.setTicketType(ticketTypeComboBox.getValue());
        form.setCountText(ticketCountField.getText() == null ? "" : ticketCountField.getText().trim());
        form.setPriceText(priceField.getText() == null ? "" : priceField.getText().trim());

        if (selectedEvent == null || form.getTicketType() == null || form.getCustomerName().isEmpty() ||
                form.getCustomerEmail().isEmpty() || form.getCountText().isEmpty() || form.getPriceText().isEmpty()) {
            showFeedback("Udfyld event, kundeinfo, antal og pris.", false);
            return;
        }

        if (!form.getCustomerEmail().contains("@") || !form.getCustomerEmail().contains(".")) {
            showFeedback("E-mailen er ikke gyldig.", false);
            return;
        }

        int count;
        double price;
        try {
            count = Integer.parseInt(form.getCountText());
            price = Double.parseDouble(form.getPriceText());
        } catch (Exception e) {
            showFeedback("Antal og pris skal være tal.", false);
            return;
        }

        if (count <= 0 || price < 0) {
            showFeedback("Antal skal være over 0 og pris mindst 0.", false);
            return;
        }

        try {
            User receiverUser = userService.findOrCreateTicketUser(form.getCustomerName(), form.getCustomerEmail());

            for (int i = 0; i < count; i++) {

                //  Unguessable ID
                String ticketId = UUID.randomUUID().toString().replace("-", "");

                Tickets ticket = new Tickets(
                        ticketId,
                        Integer.parseInt(selectedEvent.getId()),
                        receiverUser.getId(),
                        price,
                        form.getCustomerName(),
                        form.getCustomerEmail(),
                        "PENDING",
                        null,
                        currentUser.getId(),
                        form.getTicketType(),
                        null // QR-code text (Tickets generate itself)
                );

                ticketService.createTicket(ticket);
            }

            customerNameField.clear();
            customerEmailField.clear();
            ticketCountField.setText("1");
            ticketTypeComboBox.setValue(TicketType.STANDARD);
            receiverUserComboBox.getSelectionModel().clearSelection();
            loadReceiverUsers();
            loadTickets();
            showFeedback("Billetter oprettet.", true);

        } catch (Exception e) {
            showFeedback("Kunne ikke oprette billetter: " + e.getMessage(), false);
        }
    }


    @FXML
    private void handleOpenTicket() {
        Tickets selected = ticketTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showFeedback("Vælg en billet i listen.", false);
            return;
        }
        openTicketDetails(selected);
    }

    @FXML
    private void handleDeleteTicket() {
        Tickets selected = ticketTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showFeedback("Vælg en billet du vil slette.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Slet billet");
        confirm.setHeaderText(null);
        confirm.setContentText("Er du sikker på, at du vil slette denne billet?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            ticketService.deleteTicket(selected.getTicketId());
            loadTickets();
            showFeedback("Billet slettet.", true);
        } catch (Exception e) {
            showFeedback("Kunne ikke slette billet: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleClearReceiverSelection() {
        receiverUserComboBox.getSelectionModel().clearSelection();
    }

    private void loadEvents() {
        List<Event> all = eventService.getAllEvents();
        eventNameById.clear();

        for (Event event : all) {
            try {
                eventNameById.put(Integer.parseInt(event.getId()), event.getTitle());
            } catch (Exception ignored) {}
        }

        if (currentUser != null && currentUser.getRole() == UserRole.COORDINATOR) {
            all = all.stream()
                    .filter(e -> e.getOwnerCoordinatorId() == null || e.hasAccess(currentUser.getId()))
                    .toList();
        }

        eventComboBox.setItems(FXCollections.observableArrayList(all));
    }

    private void loadReceiverUsers() {
        List<User> receiverUsers = userService.getAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.USER)
                .toList();

        receiverUserComboBox.setItems(FXCollections.observableArrayList(receiverUsers));

        if (receiverUsers.isEmpty()) {
            receiverUserComboBox.setPromptText("Ingen bruger fundet - brug manuel");
            receiverUserComboBox.setDisable(true);
        } else {
            receiverUserComboBox.setPromptText("Vælg bruger (valgfri)");
            receiverUserComboBox.setDisable(false);
        }
    }

    private void loadTickets() {
        try {
            if (showOnlyFreeTickets) {
                List<Tickets> freeTickets = ticketService.getAllTickets()
                        .stream()
                        .filter(t ->
                                t.getTicketType() == TicketType.FREE ||
                                        t.getTicketType() == TicketType.FREE_BEER ||
                                        t.getTicketType() == TicketType.FREE_DRINK
                        )
                        .toList();

                ticketTable.setItems(FXCollections.observableArrayList(freeTickets));
                return;
            }

            List<Tickets> tickets;

            if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
                tickets = ticketService.getAllTickets();

            } else if (currentUser != null && currentUser.getRole() == UserRole.COORDINATOR) {

                List<Tickets> allTickets = ticketService.getAllTickets();
                Set<Integer> allowedEventIds = new HashSet<>();

                for (Event event : eventService.getAllEvents()) {
                    if (event.getOwnerCoordinatorId() == null || event.hasAccess(currentUser.getId())) {
                        allowedEventIds.add(Integer.parseInt(event.getId()));
                    }
                }

                tickets = allTickets.stream()
                        .filter(t -> allowedEventIds.contains(t.getEventId()))
                        .toList();

            } else if (currentUser != null) {
                tickets = ticketService.getTicketsForUser(currentUser.getId());

            } else {
                tickets = List.of();
            }

            ticketTable.setItems(FXCollections.observableArrayList(tickets));

        } catch (Exception e) {
            showFeedback("Kunne ikke hente billetter: " + e.getMessage(), false);
        }
    }

    private void showFeedback(String message, boolean success) {
        ticketFeedbackLabel.setText(message);
        ticketFeedbackLabel.getStyleClass().removeAll("feedback-success", "feedback-error");
        ticketFeedbackLabel.getStyleClass().add(success ? "feedback-success" : "feedback-error");
        ticketFeedbackLabel.setVisible(true);
        ticketFeedbackLabel.setManaged(true);
    }

    private void openTicketDetails(Tickets ticket) {
        TicketDetailsController.setTicket(ticket);
        ViewManager.getInstance().loadView("TicketDetailsView.fxml");
    }

    private void updateTicketTypeChoices(Event selectedEvent) {
        ticketTypeComboBox.getItems().clear();

        ticketTypeComboBox.getItems().addAll(
                TicketType.STANDARD,
                TicketType.FREE,
                TicketType.FREE_BEER,
                TicketType.FREE_DRINK
        );

        if (selectedEvent != null && selectedEvent.isVipEnabled()) {
            ticketTypeComboBox.getItems().add(TicketType.VIP);
        }

        ticketTypeComboBox.setValue(TicketType.STANDARD);
    }
}
