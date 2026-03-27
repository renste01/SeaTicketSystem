package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class EditEventController {

    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;

    private static Event eventToEdit;

    public static void setEvent(Event e) {
        eventToEdit = e;
    }

    @FXML
    private void initialize() {
        if (eventToEdit != null) {
            titleField.setText(eventToEdit.getTitle());
            locationField.setText(eventToEdit.getLocation());
            datePicker.setValue(eventToEdit.getDate());
            descriptionField.setText(eventToEdit.getDescription());
        }
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();
        LocalDate date = datePicker.getValue();
        String description = descriptionField.getText().trim();

        if (title.isEmpty() || location.isEmpty() || date == null || description.isEmpty()) {
            showError("Udfyld venligst alle felter.");
            return;
        }

        // Update event in the shared list
        EventListController.updateEvent(
                eventToEdit.getId(),
                title,
                location,
                date,
                description
        );

        ViewManager.getInstance().loadView("EventListView.fxml");
    }

    @FXML
    private void handleCancel() {
        ViewManager.getInstance().loadView("EventDetailsView.fxml");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
