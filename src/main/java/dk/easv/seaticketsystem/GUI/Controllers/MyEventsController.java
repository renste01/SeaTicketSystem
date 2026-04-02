package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MyEventsController implements Initializable {

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, String> colDate;
    @FXML private TableColumn<Event, String> colTime;
    @FXML private TableColumn<Event, String> colLocation;
    @FXML private TableColumn<Event, String> colCoordinators;
    @FXML private TableColumn<Event, Void> colInvite;

    private final UserService userService = new UserService();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = SessionManager.getInstance().getCurrentUser();

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
        loadMyEvents();
    }

    private void setupInviteColumn() {
        colInvite.setCellFactory(col -> new TableCell<>() {
            private final Button inviteBtn = new Button("👥 Inviter ko-koordinator");

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
                setGraphic(empty ? null : inviteBtn);
            }
        });
    }

    private void handleInviteCoCoordinator(Event event) {
        // Get all coordinators except the owner and already invited ones
        List<User> availableCoordinators = userService.getAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.COORDINATOR)
                .filter(u -> !u.getId().equals(event.getOwnerCoordinatorId()))
                .filter(u -> !event.getCoCoordinatorIds().contains(u.getId()))
                .collect(Collectors.toList());

        if (availableCoordinators.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ingen tilgængelige koordinatorer");
            alert.setHeaderText(null);
            alert.setContentText("Der er ingen andre koordinatorer at invitere.");
            alert.showAndWait();
            return;
        }

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Inviter Ko-koordinator");
        dialog.setHeaderText("Vælg en koordinator til: " + event.getTitle());

        ButtonType inviteButtonType = new ButtonType("Inviter", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuller", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(inviteButtonType, cancelButtonType);

        ComboBox<User> coordinatorDropdown = new ComboBox<>();
        coordinatorDropdown.getItems().addAll(availableCoordinators);
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
            eventTable.refresh();

            Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
            confirmation.setTitle("Invitation sendt");
            confirmation.setHeaderText(null);
            confirmation.setContentText(selectedUser.getName() + " er nu ko-koordinator på: " + event.getTitle());
            confirmation.showAndWait();
        });
    }

    private void loadMyEvents() {
        if (currentUser == null) return;

        // Only show events this coordinator owns
        List<Event> myEvents = EventListController.getEvents().stream()
                .filter(e -> e.isOwnedBy(currentUser.getId()))
                .collect(Collectors.toList());

        eventTable.getItems().setAll(myEvents);
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
}