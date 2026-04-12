package dk.easv.seaticketsystem.GUI.Controllers;

// Projekt Imports
import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;

// Java Imports
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CreateEventController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextArea descriptionField;
    @FXML private TextArea locationGuidanceField;
    @FXML private CheckBox vipEnabledCheckBox;
    @FXML private ListView<User> coordinatorList;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getId() : null;

        List<User> coordinators = new ArrayList<>();
        for (User user : userService.getAllUsers()) {
            if (user.getRole() == UserRole.COORDINATOR &&
                    (currentUserId == null || !currentUserId.equals(user.getId()))) {
                coordinators.add(user);
            }
        }
        coordinatorList.getItems().setAll(coordinators);
        coordinatorList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void handleCreate() {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();
        LocalDate date = datePicker.getValue();
        String startTimeText = startTimeField.getText().trim();
        String endTimeText = endTimeField.getText().trim();
        String description = descriptionField.getText().trim();
        String locationGuidance = locationGuidanceField.getText().trim();
        boolean vipEnabled = vipEnabledCheckBox.isSelected();

        if (title.isEmpty() || location.isEmpty() || date == null || description.isEmpty()
                || startTimeText.isEmpty() || endTimeText.isEmpty() || locationGuidance.isEmpty()) {
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
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);
        if (!endDateTime.isAfter(startDateTime)) {
            showError("Sluttid skal være efter starttid.");
            return;
        }

        // Tag event with the current coordinator's ID
        User currentUser = SessionManager.getInstance().getCurrentUser();
        String ownerId = currentUser != null ? currentUser.getId() : null;

        Event newEvent = new Event(
                "0",
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

        List<User> selectedCoordinators = coordinatorList.getSelectionModel().getSelectedItems();
        for (User coordinator : selectedCoordinators) {
            if (ownerId == null || !ownerId.equals(coordinator.getId())) {
                newEvent.addCoCoordinator(coordinator.getId());
            }
        }

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
