package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MyEventsController implements Initializable {

    @FXML private VBox eventCardsContainer;

    private final UserService userService = new UserService();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        loadMyEvents();
    }

    private void loadMyEvents() {
        if (currentUser == null) return;

        eventCardsContainer.getChildren().clear();

        List<Event> myEvents = EventListController.getEvents().stream()
                .filter(e -> e.isOwnedBy(currentUser.getId()))
                .toList();

        if (myEvents.isEmpty()) {
            Label empty = new Label("Du har ingen events endnu. Opret et event for at komme i gang.");
            empty.setStyle("-fx-text-fill: #888; -fx-font-size: 13px;");
            eventCardsContainer.getChildren().add(empty);
            return;
        }

        for (Event event : myEvents) {
            eventCardsContainer.getChildren().add(buildEventCard(event));
        }
    }

    private VBox buildEventCard(Event event) {
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #e4e8ec;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 2);"
        );
        card.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label(event.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #002430;");

        // Meta info
        HBox metaRow = new HBox(24);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.getChildren().addAll(
                metaLabel("📅  " + event.getDate()),
                metaLabel("📍  " + event.getLocation())
        );

        // Co-coordinators
        List<String> coIds = event.getCoCoordinatorIds();
        String coNames = coIds.isEmpty() ? "Ingen ko-koordinatorer endnu" :
                coIds.stream()
                        .map(id -> userService.getAllUsers().stream()
                                .filter(u -> u.getId().equals(id))
                                .findFirst()
                                .map(User::getName)
                                .orElse("Ukendt"))
                        .collect(java.util.stream.Collectors.joining(", "));

        Label coLabel = new Label("👥  Ko-koordinatorer: " + coNames);
        coLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #454b50;");
        coLabel.setWrapText(true);

        // Invite button
        Button inviteBtn = new Button("👥  Inviter ko-koordinator");
        inviteBtn.setStyle(
                "-fx-background-color: #3c7d87;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 8 20 8 20;" +
                        "-fx-cursor: hand;"
        );
        inviteBtn.setOnAction(_ -> {
            handleInviteCoCoordinator(event);
            int index = eventCardsContainer.getChildren().indexOf(card);
            if (index >= 0) {
                eventCardsContainer.getChildren().set(index, buildEventCard(event));
            }
        });

        Separator sep = new Separator();

        card.getChildren().addAll(titleLabel, sep, metaRow, coLabel, inviteBtn);
        return card;
    }

    private Label metaLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #454b50;");
        return label;
    }

    private void handleInviteCoCoordinator(Event event) {
        List<User> availableCoordinators = userService.getAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.COORDINATOR)
                .filter(u -> !u.getId().equals(event.getOwnerCoordinatorId()))
                .filter(u -> !event.getCoCoordinatorIds().contains(u.getId()))
                .toList();

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
        coordinatorDropdown.setCellFactory(_ -> new ListCell<>() {
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
        coordinatorDropdown.valueProperty().addListener((_, _, newVal) ->
                inviteButton.setDisable(newVal == null));

        VBox content = new VBox(8, new Label("Ko-koordinator:"), coordinatorDropdown);
        content.setPrefWidth(380);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(buttonType ->
                buttonType == inviteButtonType ? coordinatorDropdown.getValue() : null);

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(selectedUser -> {
            event.addCoCoordinator(selectedUser.getId());

            Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
            confirmation.setTitle("Invitation sendt");
            confirmation.setHeaderText(null);
            confirmation.setContentText(selectedUser.getName() + " er nu ko-koordinator på: " + event.getTitle());
            confirmation.showAndWait();
        });
    }
}