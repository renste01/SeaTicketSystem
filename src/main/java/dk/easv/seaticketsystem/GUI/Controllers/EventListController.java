package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EventListController implements Initializable {

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, String> colDate;
    @FXML private TableColumn<Event, String> colTime;
    @FXML private TableColumn<Event, String> colLocation;
    @FXML private TableColumn<Event, String> colCoordinators;
    @FXML private TableColumn<Event, Void> colInvite;
    @FXML private TableColumn<Event, Void> colDelete;

    private final UserService userService = new UserService();

    private static final List<Event> events = new ArrayList<>(List.of(
            new Event("1", "Koncert i Havnen", "Esbjerg Havn", LocalDate.of(2025, 6, 12), LocalTime.of(18, 0), "Live concert at the harbor", LocalDateTime.of(2025, 6, 12, 22, 0)),
            new Event("2", "Sommerfestival", "Musikhuset", LocalDate.of(2025, 7, 3), LocalTime.of(19, 0), "Summer festival with music and food", LocalDateTime.of(2025, 7, 3, 23, 30))
    ));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate().toString()));
        colTime.setCellValueFactory(c -> {
            LocalDateTime end = c.getValue().getEndDateTime();
            if (end == null) return new SimpleStringProperty("-");
            return new SimpleStringProperty(end.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        });
        colLocation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));
        colCoordinators.setCellValueFactory(c -> new SimpleStringProperty(getCoordinatorNames(c.getValue())));

        setupInviteColumn();
        setupDeleteColumn();
        loadEvents();

        eventTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                Event selected = eventTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    EventDetailsController.setEvent(selected);
                    ViewManager.getInstance().loadView("EventDetailsView.fxml");
                }
            }
        });
    }

    private void setupInviteColumn() {
        colInvite.setCellFactory(col -> new TableCell<>() {
            private final Button inviteBtn = new Button("👥 Inviter");

            {
                inviteBtn.setStyle("-fx-background-color: #3c7d87; -fx-text-fill: white; -fx-cursor: hand;");
                inviteBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    handleInviteCoCoordinator(event);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Event event = getTableView().getItems().get(getIndex());
                User currentUser = SessionManager.getInstance().getCurrentUser();

                // Only show invite button if current user owns this event
                boolean isOwner = currentUser != null && event.isOwnedBy(currentUser.getId());
                setGraphic(isOwner ? inviteBtn : null);
            }
        });
    }

    private void handleInviteCoCoordinator(Event event) {
        // Get all coordinators except the owner
        List<User> allCoordinators = userService.getAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.COORDINATOR)
                .filter(u -> !u.getId().equals(event.getOwnerCoordinatorId()))
                .filter(u -> !event.getCoCoordinatorIds().contains(u.getId()))
                .collect(Collectors.toList());

        if (allCoordinators.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ingen tilgængelige koordinatorer");
            alert.setHeaderText(null);
            alert.setContentText("Der er ingen andre koordinatorer at invitere.");
            alert.showAndWait();
            return;
        }

        // Build dialog with dropdown
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Inviter Ko-koordinator");
        dialog.setHeaderText("Vælg en koordinator til: " + event.getTitle());

        ButtonType inviteButtonType = new ButtonType("Inviter", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuller", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(inviteButtonType, cancelButtonType);

        ComboBox<User> coordinatorDropdown = new ComboBox<>();
        coordinatorDropdown.getItems().addAll(allCoordinators);
        coordinatorDropdown.setPromptText("Vælg koordinator...");
        coordinatorDropdown.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getName() + " (" + user.getEmail() + ")");
            }
        });
        coordinatorDropdown.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getName() + " (" + user.getEmail() + ")");
            }
        });

        // Disable invite button until selection is made
        javafx.scene.Node inviteButton = dialog.getDialogPane().lookupButton(inviteButtonType);
        inviteButton.setDisable(true);
        coordinatorDropdown.valueProperty().addListener((obs, oldVal, newVal) ->
                inviteButton.setDisable(newVal == null));

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(8,
                new Label("Ko-koordinator:"), coordinatorDropdown);
        content.setPrefWidth(380);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == inviteButtonType) return coordinatorDropdown.getValue();
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(selectedUser -> {
            event.addCoCoordinator(selectedUser.getId());
            eventTable.refresh();
            System.out.println("Invited " + selectedUser.getName() + " as co-coordinator for: " + event.getTitle());

            Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
            confirmation.setTitle("Invitation sendt");
            confirmation.setHeaderText(null);
            confirmation.setContentText(selectedUser.getName() + " er nu ko-koordinator på: " + event.getTitle());
            confirmation.showAndWait();
        });
    }

    private void setupDeleteColumn() {
        colDelete.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑 Slet");

            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    Event event = getTableView().getItems().get(getIndex());
                    handleDeleteEvent(event);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Event event = getTableView().getItems().get(getIndex());
                User currentUser = SessionManager.getInstance().getCurrentUser();
                UserRole role = currentUser != null ? currentUser.getRole() : null;

                // Admins can delete any event, coordinators only their own
                boolean canDelete = role == UserRole.ADMIN ||
                        (role == UserRole.COORDINATOR && event.hasAccess(currentUser.getId()));
                setGraphic(canDelete ? deleteBtn : null);
            }
        });
    }

    private void handleDeleteEvent(Event event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Slet Event");
        dialog.setHeaderText("Du er ved at slette: " + event.getTitle());

        ButtonType deleteButtonType = new ButtonType("Slet", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuller", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, cancelButtonType);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Skriv en begrundelse for sletningen...");
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(3);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(8,
                new Label("Begrundelse (påkrævet):"), commentArea);
        content.setPrefWidth(400);
        dialog.getDialogPane().setContent(content);

        javafx.scene.Node deleteButton = dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.setDisable(true);
        commentArea.textProperty().addListener((obs, oldVal, newVal) ->
                deleteButton.setDisable(newVal.trim().isEmpty()));

        dialog.setResultConverter(buttonType -> {
            if (buttonType == deleteButtonType) return commentArea.getText().trim();
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(comment -> {
            events.remove(event);
            eventTable.getItems().remove(event);
            System.out.println("Event '" + event.getTitle() + "' slettet. Begrundelse: " + comment);
        });
    }

    public static void addEvent(Event e) {
        events.add(e);
    }

    public static void updateEvent(String id, String title, String location, LocalDate date, LocalTime startTime, String description, LocalDateTime endDateTime, String locationGuidance) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(id)) {
                Event old = events.get(i);
                Event updated = new Event(id, title, location, date, startTime, description, old.getOwnerCoordinatorId(), endDateTime, locationGuidance);
                old.getCoCoordinatorIds().forEach(updated::addCoCoordinator);
                events.set(i, updated);
                break;
            }
        }
    }
    public static void deleteEvent(String id) {
        events.removeIf(e -> e.getId().equals(id));
    }

    public static List<Event> getEvents() {
        return new ArrayList<>(events);
    }

    private String getCoordinatorNames(Event event) {
        List<User> allUsers = userService.getAllUsers();
        List<String> names = new ArrayList<>();

        String ownerId = event.getOwnerCoordinatorId();
        if (ownerId != null) {
            for (User user : allUsers) {
                if (user.getId().equals(ownerId)) {
                    names.add(user.getName());
                    break;
                }
            }
        }

        for (String coId : event.getCoCoordinatorIds()) {
            for (User user : allUsers) {
                if (user.getId().equals(coId)) {
                    names.add(user.getName());
                    break;
                }
            }
        }

        if (names.isEmpty()) return "Ingen";
        return String.join(", ", names);
    }

    private void loadEvents() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        List<Event> visible;
        if (currentUser.getRole() == UserRole.ADMIN) {
            // Admins see everything
            visible = new ArrayList<>(events);
        } else {
            // Coordinators only see their own or events they are co-coordinator on
            visible = events.stream()
                    .filter(e -> e.getOwnerCoordinatorId() == null || e.hasAccess(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        eventTable.getItems().setAll(visible);
    }
}
