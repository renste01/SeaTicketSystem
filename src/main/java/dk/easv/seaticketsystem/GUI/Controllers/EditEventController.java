package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EditEventController {

    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextArea descriptionField;
    @FXML private TextArea locationGuidanceField;
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
            if (eventToEdit.getStartTime() != null) {
                startTimeField.setText(eventToEdit.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            if (eventToEdit.getEndDateTime() != null) {
                endDatePicker.setValue(eventToEdit.getEndDateTime().toLocalDate());
                endTimeField.setText(eventToEdit.getEndDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            descriptionField.setText(eventToEdit.getDescription());
            locationGuidanceField.setText(eventToEdit.getLocationGuidance());
        }
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();
        LocalDate date = datePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String startTimeText = startTimeField.getText().trim();
        String endTimeText = endTimeField.getText().trim();
        String description = descriptionField.getText().trim();
        String locationGuidance = locationGuidanceField.getText().trim();

        if (title.isEmpty() || location.isEmpty() || date == null || description.isEmpty()
                || endDate == null || startTimeText.isEmpty() || endTimeText.isEmpty() || locationGuidance.isEmpty()) {
            showError("Udfyld venligst alle felter.");
            return;
        }

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(startTimeText);
            endTime = LocalTime.parse(endTimeText);
        } catch (Exception ex) {
            showError("Start/sluttid skal være i format HH:mm.");
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
        if (!endDateTime.isAfter(startDateTime)) {
            showError("Slutdato/-tid skal være efter startdato.");
            return;
        }

        // Update event in the shared list
        EventListController.updateEvent(
                eventToEdit.getId(),
                title,
                location,
                date,
                startTime,
                description,
                endDateTime,
                locationGuidance
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
