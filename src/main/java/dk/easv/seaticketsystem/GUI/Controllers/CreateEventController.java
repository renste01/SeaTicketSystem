package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.UUID;

public class CreateEventController {

    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();
        LocalDate date = datePicker.getValue();
        String description = descriptionField.getText().trim();

        if (title.isEmpty() || location.isEmpty() || date == null || description.isEmpty()) {
            showError("Udfyld venligst alle felter.");
            return;
        }

        Event newEvent = new Event(
                UUID.randomUUID().toString(),
                title,
                location,
                date,
                description
        );

        // Add event to the shared list
        EventListController.addEvent(newEvent);

        // Navigate back to event list
        ViewManager.getInstance().loadView("EventListView.fxml");
    }

    @FXML
    private void handleCancel() {
        ViewManager.getInstance().loadView("EventListView.fxml");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
