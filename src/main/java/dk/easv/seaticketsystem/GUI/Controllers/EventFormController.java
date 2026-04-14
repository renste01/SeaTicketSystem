package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.EventService;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Session.SessionManager;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.UUID;

public class EventFormController implements Initializable {

    @FXML private Label formTitleLabel;
    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField locationGuidanceField;
    @FXML private CheckBox vipCheckBox;
    @FXML private Button saveButton;
    @FXML private Label errorLabel;

    private static Event eventToEdit = null;

    public static void setEventToEdit(Event event) {
        eventToEdit = event;
    }

    public static void clearEventToEdit() {
        eventToEdit = null;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (eventToEdit != null) {
            // Edit mode
            formTitleLabel.setText("Rediger Event");
            saveButton.setText("💾 Gem ændringer");
            titleField.setText(eventToEdit.getTitle());
            locationField.setText(eventToEdit.getLocation());
            datePicker.setValue(eventToEdit.getDate());
            descriptionField.setText(eventToEdit.getDescription());

            if (eventToEdit.getEndDateTime() != null) {
                endDatePicker.setValue(eventToEdit.getEndDateTime().toLocalDate());
                endTimeField.setText(eventToEdit.getEndDateTime().toLocalTime().toString());
            }
            if (eventToEdit.getStartTime() != null) {
                startTimeField.setText(eventToEdit.getStartTime().toString());
            }
            if (eventToEdit.getLocationGuidance() != null) {
                locationGuidanceField.setText(eventToEdit.getLocationGuidance());
            }
            vipCheckBox.setSelected(eventToEdit.isVipEnabled());
        } else {
            // Create mode
            formTitleLabel.setText("Opret Event");
            saveButton.setText("✅ Opret Event");
        }
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();
        LocalDate date = datePicker.getValue();
        String description = descriptionField.getText().trim();
        String locationGuidance = locationGuidanceField.getText().trim();
        boolean vipEnabled = vipCheckBox.isSelected();

        if (title.isEmpty() || location.isEmpty() || date == null || description.isEmpty()) {
            showError("Udfyld venligst titel, sted, dato og beskrivelse.");
            return;
        }

        // Parse start time
        LocalTime startTime = null;
        String startTimeText = startTimeField.getText().trim();
        if (!startTimeText.isEmpty()) {
            try {
                startTime = LocalTime.parse(startTimeText);
            } catch (Exception e) {
                showError("Starttid skal være i formatet HH:mm (fx 14:00).");
                return;
            }
        }

        // Parse end datetime
        LocalDateTime endDateTime = null;
        LocalDate endDate = endDatePicker.getValue();
        String endTimeText = endTimeField.getText().trim();
        if (endDate != null && !endTimeText.isEmpty()) {
            try {
                endDateTime = LocalDateTime.of(endDate, LocalTime.parse(endTimeText));
            } catch (Exception e) {
                showError("Sluttid skal være i formatet HH:mm (fx 18:00).");
                return;
            }
        }

        if (eventToEdit != null) {
            // Edit mode
            EventListController.updateEvent(
                    eventToEdit.getId(),
                    title,
                    location,
                    date,
                    startTime,
                    description,
                    endDateTime,
                    locationGuidance,
                    vipEnabled
            );
            eventToEdit = null;
        } else {
            // Create mode
            User currentUser = SessionManager.getInstance().getCurrentUser();
            String ownerId = currentUser != null ? currentUser.getId() : null;

            Event newEvent = new Event(
                    UUID.randomUUID().toString(),
                    title,
                    location,
                    date,
                    startTime,
                    description,
                    ownerId,
                    endDateTime,
                    locationGuidance,
                    vipEnabled
            );
            EventListController.addEvent(newEvent);
        }

        ViewManager.getInstance().loadView("EventListView.fxml");
    }

    @FXML
    private void handleCancel() {
        boolean wasEditing = eventToEdit != null;
        eventToEdit = null;
        ViewManager.getInstance().loadView(wasEditing ? "EventDetailsView.fxml" : "EventListView.fxml");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}