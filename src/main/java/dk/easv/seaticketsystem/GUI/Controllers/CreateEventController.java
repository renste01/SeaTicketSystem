package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Session.SessionManager;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class CreateEventController {

    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField endTimeField;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();
        LocalDate date = datePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String endTimeText = endTimeField.getText().trim();
        String description = descriptionField.getText().trim();

        if (title.isEmpty() || location.isEmpty() || date == null || description.isEmpty()
                || endDate == null || endTimeText.isEmpty()) {
            showError("Udfyld venligst alle felter.");
            return;
        }

        LocalTime endTime;
        try {
            endTime = LocalTime.parse(endTimeText);
        } catch (Exception ex) {
            showError("Sluttid skal være i format HH:mm.");
            return;
        }
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
        if (!endDateTime.isAfter(startDateTime)) {
            showError("Slutdato/-tid skal være efter startdato.");
            return;
        }

        // Tag event with the current coordinator's ID
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String ownerId = currentUser != null ? currentUser.getId() : null;

        Event newEvent = new Event(
                UUID.randomUUID().toString(),
                title,
                location,
                date,
                description,
                ownerId,
                endDateTime
        );

        EventListController.addEvent(newEvent);
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
