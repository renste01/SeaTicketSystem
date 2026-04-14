package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EditEventController {

    @FXML private Label errorLabel;

    private static Event eventToEdit;

    public static void setEvent(Event e) {
        EventFormController.setEventToEdit(e);
        ViewManager.getInstance().loadView("EventFormView.fxml");
    }

    @FXML
    private void handleCancel() {
        ViewManager.getInstance().loadView("EventDetailsView.fxml");
    }
}