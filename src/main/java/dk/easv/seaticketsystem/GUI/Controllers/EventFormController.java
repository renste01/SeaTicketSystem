package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.EventService;
import dk.easv.seaticketsystem.BE.Event;
import dk.easv.seaticketsystem.Model.EventFormModel;
import dk.easv.seaticketsystem.Session.SessionManager;
import dk.easv.seaticketsystem.BE.User;
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
        EventFormModel form = new EventFormModel();
        form.setTitle(titleField.getText() == null ? "" : titleField.getText().trim());
        form.setLocation(locationField.getText() == null ? "" : locationField.getText().trim());
        form.setStartDate(datePicker.getValue());
        form.setEndDate(endDatePicker.getValue());
        form.setStartTimeText(startTimeField.getText() == null ? "" : startTimeField.getText().trim());
        form.setEndTimeText(endTimeField.getText() == null ? "" : endTimeField.getText().trim());
        form.setDescription(descriptionField.getText() == null ? "" : descriptionField.getText().trim());
        form.setLocationGuidance(locationGuidanceField.getText() == null ? "" : locationGuidanceField.getText().trim());
        form.setVipEnabled(vipCheckBox.isSelected());

        if (form.getTitle().isEmpty() || form.getLocation().isEmpty() || form.getStartDate() == null || form.getDescription().isEmpty()) {
            showError("Udfyld venligst titel, sted, dato og beskrivelse.");
            return;
        }

        // Parse start time
        LocalTime startTime = null;
        String startTimeText = form.getStartTimeText();
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
        LocalDate endDate = form.getEndDate();
        String endTimeText = form.getEndTimeText();
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
                    form.getTitle(),
                    form.getLocation(),
                    form.getStartDate(),
                    startTime,
                    form.getDescription(),
                    endDateTime,
                    form.getLocationGuidance(),
                    form.isVipEnabled()
            );
            eventToEdit = null;
        } else {
            // Create mode
            User currentUser = SessionManager.getInstance().getCurrentUser();
            String ownerId = currentUser != null ? currentUser.getId() : null;

            Event newEvent = new Event(
                    UUID.randomUUID().toString(),
                    form.getTitle(),
                    form.getLocation(),
                    form.getStartDate(),
                    startTime,
                    form.getDescription(),
                    ownerId,
                    endDateTime,
                    form.getLocationGuidance(),
                    form.isVipEnabled()
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
