package dk.easv.seaticketsystem.GUI.Controllers;

// Project Imports
import dk.easv.seaticketsystem.Model.Event;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

// Java Imports
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EventListController implements Initializable {

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, String> colDate;
    @FXML private TableColumn<Event, String> colLocation;
    @FXML private TableColumn<Event, Void> colDelete;

    private static final List<Event> events = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate().toString()));
        colLocation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));

        setupDeleteColumn();
        loadDummyEvents();
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

        Label commentLabel = new Label("Begrundelse (påkrævet):");
        Label errorLabel = new Label("Du skal angive en begrundelse.");
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(8, commentLabel, commentArea, errorLabel);
        content.setPrefWidth(400);
        dialog.getDialogPane().setContent(content);

        javafx.scene.Node deleteButton = dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.setDisable(true);
        commentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            deleteButton.setDisable(newVal.trim().isEmpty());
        });

        dialog.setResultConverter(buttonType -> {
            if (buttonType == deleteButtonType) {
                return commentArea.getText().trim();
            }
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

    private void loadDummyEvents() {
        if (events.isEmpty()) {
            events.add(new Event("1", "Koncert i Havnen", "Esbjerg Havn",
                    LocalDate.of(2025, 6, 12), "Live concert at the harbor"));

            events.add(new Event("2", "Sommerfestival", "Musikhuset",
                    LocalDate.of(2025, 7, 3), "Summer festival with music and food"));
        }
        eventTable.getItems().setAll(events);
    }
}