package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Session.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminEventManagerController implements Initializable {

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, String> colDate;
    @FXML private TableColumn<Event, String> colLocation;
    @FXML private TableColumn<Event, String> colOwner;
    @FXML private TableColumn<Event, Void> colDelete;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate().toString()));
        colLocation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));
        colOwner.setCellValueFactory(c -> {
            String ownerId = c.getValue().getOwnerCoordinatorId();
            if (ownerId == null) return new SimpleStringProperty("Ingen ejer");

            // Look up the coordinator's name by their ID
            List<User> users = userService.getAllUsers();
            return users.stream()
                    .filter(u -> u.getId().equals(ownerId))
                    .findFirst()
                    .map(u -> new SimpleStringProperty(u.getName()))
                    .orElse(new SimpleStringProperty("Ukendt koordinator"));
        });

        setupDeleteColumn();
        loadEvents();
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
                setGraphic(empty ? null : deleteBtn);
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
            EventListController.deleteEvent(event.getId());
            eventTable.getItems().remove(event);
            System.out.println("Admin slettet event '" + event.getTitle() + "'. Begrundelse: " + comment);
        });
    }

    private void loadEvents() {
        eventTable.getItems().setAll(EventListController.getEvents());
    }
}