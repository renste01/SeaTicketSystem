package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class EventDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label locationLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Button editButton;

    private static Event selectedEvent;

    public static void setEvent(Event e) {
        selectedEvent = e;
    }

    @FXML
    private void initialize() {

        if (selectedEvent != null) {
            titleLabel.setText(selectedEvent.getTitle());
            dateLabel.setText(selectedEvent.getDate().toString());
            locationLabel.setText(selectedEvent.getLocation());
            descriptionArea.setText(selectedEvent.getDescription());
        }

        // Get logged-in user
        User user = SessionManager.getInstance().getCurrentUser();
        System.out.println("Logged in role = " + user.getRole()); // Debug

        // Hide edit button unless coordinator
        if (user == null || user.getRole() != UserRole.COORDINATOR) {
            editButton.setVisible(false);
            editButton.setManaged(false);
        }
    }

    @FXML
    private void handleEdit() {
        EditEventController.setEvent(selectedEvent);
        ViewManager.getInstance().loadView("EditEventView.fxml");
    }

    @FXML
    private void handleBack() {
        ViewManager.getInstance().loadView("EventListView.fxml");
    }
}
