package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.EventService;
import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EventListController implements Initializable {

    @FXML private VBox eventCardsContainer;

    private final UserService userService = new UserService();
    private final EventService eventService = new EventService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadEvents();
    }

    private void handleInviteCoCoordinator(Event event) {
        List<User> allCoordinators = new ArrayList<>();
        for (User user : userService.getAllUsers()) {
            if (user.getRole() != UserRole.COORDINATOR) continue;
            if (user.getId().equals(event.getOwnerCoordinatorId())) continue;
            if (event.getCoCoordinatorIds().contains(user.getId())) continue;
            allCoordinators.add(user);
        }

        if (allCoordinators.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ingen tilgængelige koordinatorer");
            alert.setHeaderText(null);
            alert.setContentText("Der er ingen andre koordinatorer at tilføje.");
            alert.showAndWait();
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Tilføj koordinator");
        dialog.setHeaderText("Vælg en koordinator til: " + event.getTitle());

        ButtonType inviteButtonType = new ButtonType("Tilføj", ButtonBar.ButtonData.OK_DONE);
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
            loadEvents();

            Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
            confirmation.setTitle("Invitation sendt");
            confirmation.setHeaderText(null);
            confirmation.setContentText(selectedUser.getName() + " er nu ko-koordinator på: " + event.getTitle());
            confirmation.showAndWait();
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
            eventService.deleteEvent(event.getId());
            loadEvents();
            System.out.println("Event '" + event.getTitle() + "' slettet. Begrundelse: " + comment);
        });
    }

    public static void addEvent(Event e) {
        new EventService().createEvent(e);
    }

    public static void updateEvent(String id, String title, String location, LocalDate date, LocalTime startTime, String description, LocalDateTime endDateTime, String locationGuidance, boolean vipEnabled) {
        EventService service = new EventService();
        List<Event> all = service.getAllEvents();
        for (Event old : all) {
            if (old.getId().equals(id)) {
                Event updated = new Event(id, title, location, date, startTime, description, old.getOwnerCoordinatorId(), endDateTime, locationGuidance, vipEnabled);
                old.getCoCoordinatorIds().forEach(updated::addCoCoordinator);
                service.updateEvent(updated);
                break;
            }
        }
    }

    public static void deleteEvent(String id) {
        new EventService().deleteEvent(id);
    }

    public static List<Event> getEvents() {
        return new EventService().getAllEvents();
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

        eventCardsContainer.getChildren().clear();

        List<Event> allEvents = eventService.getAllEvents();
        List<Event> visible = new ArrayList<>();
        if (currentUser.getRole() == UserRole.ADMIN) {
            visible.addAll(allEvents);
        } else {
            for (Event event : allEvents) {
                if (event.getOwnerCoordinatorId() == null || event.hasAccess(currentUser.getId())) {
                    visible.add(event);
                }
            }
        }

        if (visible.isEmpty()) {
            Label empty = new Label("Ingen events fundet.");
            empty.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
            eventCardsContainer.getChildren().add(empty);
            return;
        }

        for (Event event : visible) {
            eventCardsContainer.getChildren().add(buildEventCard(event, currentUser));
        }
    }

    private VBox buildEventCard(Event event, User currentUser) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");
        card.setPadding(new Insets(14));

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("event-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        titleRow.getChildren().addAll(titleLabel, spacer);

        HBox metaRow = new HBox(18);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label dateLabel = new Label("📅 " + event.getDate());
        dateLabel.getStyleClass().add("event-card-meta");
        Label timeLabel = new Label("⏰ " + event.getTimeRangeDisplay());
        timeLabel.getStyleClass().add("event-card-meta");
        Label locationLabel = new Label("📍 " + event.getLocation());
        locationLabel.getStyleClass().add("event-card-meta");
        metaRow.getChildren().addAll(dateLabel, timeLabel, locationLabel);

        Label coordinatorLabel = new Label("👤 Koordinatorer: " + getCoordinatorNames(event));
        coordinatorLabel.getStyleClass().add("event-card-meta");
        coordinatorLabel.setWrapText(true);

        HBox actionsRow = new HBox(8);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        Button detailsBtn = new Button("Se");
        detailsBtn.getStyleClass().add("btn-secondary");
        detailsBtn.setOnAction(e -> openEvent(event));

        actionsRow.getChildren().add(detailsBtn);

        boolean isOwner = currentUser != null && event.isOwnedBy(currentUser.getId());
        if (isOwner) {
            Button inviteBtn = new Button("Tilføj koordinator");
            inviteBtn.getStyleClass().add("btn-teal");
            inviteBtn.setOnAction(e -> handleInviteCoCoordinator(event));
            actionsRow.getChildren().add(inviteBtn);
        }

        boolean canDelete = currentUser != null && (currentUser.getRole() == UserRole.ADMIN ||
                (currentUser.getRole() == UserRole.COORDINATOR && event.hasAccess(currentUser.getId())));
        if (canDelete) {
            Button deleteBtn = new Button("Slet");
            deleteBtn.getStyleClass().add("btn-danger");
            deleteBtn.setOnAction(e -> handleDeleteEvent(event));
            actionsRow.getChildren().add(deleteBtn);
        }

        card.getChildren().addAll(titleRow, metaRow, coordinatorLabel, actionsRow);
        card.setOnMouseClicked(e -> openEvent(event));
        return card;
    }

    private void openEvent(Event event) {
        EventDetailsController.setEvent(event);
        ViewManager.getInstance().loadView("EventDetailsView.fxml");
    }

}
