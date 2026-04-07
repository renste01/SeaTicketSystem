package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.EventService;
import dk.easv.seaticketsystem.BLL.TicketService;
import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.Tickets;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MyTicketsController {

    @FXML private ComboBox<Event> eventComboBox;
    @FXML private ComboBox<User> receiverUserComboBox;
    @FXML private TextField customerNameField;
    @FXML private TextField customerEmailField;
    @FXML private TextField ticketCountField;
    @FXML private TextField priceField;
    @FXML private Label ticketFeedbackLabel;

    @FXML private TableView<Tickets> ticketTable;
    @FXML private TableColumn<Tickets, String> colEvent;
    @FXML private TableColumn<Tickets, String> colCustomer;
    @FXML private TableColumn<Tickets, String> colEmail;
    @FXML private TableColumn<Tickets, Double> colPrice;
    @FXML private TableColumn<Tickets, String> colStatus;
    @FXML private TableColumn<Tickets, String> colSentAt;

    private final TicketService ticketService = new TicketService();
    private final EventService eventService = new EventService();
    private final UserService userService = new UserService();
    private User currentUser;
    private final java.util.Map<Integer, String> eventNameById = new java.util.HashMap<>();

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
                setText(empty || item == null ? null : item.getTitle() + " (" + item.getDate() + ")");
            }
        });
        eventComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " (" + item.getDate() + ")");
            }
        });

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

        loadEvents();
        loadReceiverUsers();
        loadTickets();
    }

    @FXML
    private void handleIssueTickets() {
        Event selectedEvent = eventComboBox.getValue();
        String customerName = customerNameField.getText() == null ? "" : customerNameField.getText().trim();
        String customerEmail = customerEmailField.getText() == null ? "" : customerEmailField.getText().trim();
        String countText = ticketCountField.getText() == null ? "" : ticketCountField.getText().trim();
        String priceText = priceField.getText() == null ? "" : priceField.getText().trim();

        if (selectedEvent == null || customerName.isEmpty() || customerEmail.isEmpty() || countText.isEmpty() || priceText.isEmpty()) {
            showFeedback("Udfyld event, kundeinfo, antal og pris.", false);
            return;
        }
        if (!customerEmail.contains("@") || !customerEmail.contains(".")) {
            showFeedback("E-mailen er ikke gyldig.", false);
            return;
        }

        int count;
        double price;
        try {
            count = Integer.parseInt(countText);
            price = Double.parseDouble(priceText);
        } catch (Exception e) {
            showFeedback("Antal og pris skal være tal.", false);
            return;
        }

        if (count <= 0 || price < 0) {
            showFeedback("Antal skal være over 0 og pris mindst 0.", false);
            return;
        }
        if (currentUser == null) {
            showFeedback("Ingen bruger er logget ind.", false);
            return;
        }

        try {
            for (int i = 0; i < count; i++) {
                Tickets ticket = new Tickets(
                        UUID.randomUUID().toString(),
                        Integer.parseInt(selectedEvent.getId()),
                        currentUser.getId(),
                        price,
                        customerName,
                        customerEmail,
                        "PENDING",
                        null,
                        currentUser.getId()
                );
                ticketService.createTicket(ticket);
            }

            customerNameField.clear();
            customerEmailField.clear();
            ticketCountField.setText("1");
            loadTickets();
            showFeedback("Billetter oprettet (klar til afsendelse/print).", true);
        } catch (Exception e) {
            showFeedback("Kunne ikke oprette billetter: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleMarkAsSent() {
        Tickets selected = ticketTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showFeedback("Vælg en billet i listen foerst.", false);
            return;
        }
        if ("SENT".equalsIgnoreCase(selected.getDeliveryStatus())) {
            showFeedback("Denne billet er allerede markeret som sendt.", false);
            return;
        }

        try {
            ticketService.markTicketAsSent(selected.getTicketId());
            loadTickets();
            showFeedback("Billet markeret som sendt.", true);
        } catch (Exception e) {
            showFeedback("Kunne ikke opdatere billet: " + e.getMessage(), false);
        }
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

    @FXML
    private void handleClearReceiverSelection() {
        receiverUserComboBox.getSelectionModel().clearSelection();
    }

    private void loadTickets() {
        try {
            List<Tickets> tickets;
            if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
                tickets = ticketService.getAllTickets();
            } else if (currentUser != null && currentUser.getRole() == UserRole.COORDINATOR) {
                List<Tickets> allTickets = ticketService.getAllTickets();
                Set<Integer> allowedEventIds = new HashSet<>();
                for (Event event : eventService.getAllEvents()) {
                    if (event.getOwnerCoordinatorId() == null || event.hasAccess(currentUser.getId())) {
                        try {
                            allowedEventIds.add(Integer.parseInt(event.getId()));
                        } catch (Exception ignored) {}
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
}
