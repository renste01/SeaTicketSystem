package dk.easv.seaticketsystem.GUI.Controllers;
// Project Imports
import dk.easv.seaticketsystem.GUI.Util.ViewManager;
import dk.easv.seaticketsystem.Model.Event;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
// Java Imports
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EventListController implements Initializable {

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> colTitle;
    @FXML private TableColumn<Event, String> colDate;
    @FXML private TableColumn<Event, String> colLocation;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate().toString()));
        colLocation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));

        eventTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Event selected = eventTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    EventDetailsController.setEvent(selected);
                    ViewManager.getInstance().loadView("EventDetailsView.fxml");
                }
            }
        });

        loadDummyEvents();
    }

    private static final List<Event> events = new ArrayList<>();

    public static void addEvent(Event e) {
        events.add(e);
    }
    public static void updateEvent(String id, String title, String location, LocalDate date, String description) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(id)) {
                events.set(i, new Event(id, title, location, date, description));
                break;
            }
        }
    }

    private void loadDummyEvents() {
        if (events.isEmpty()) {
            events.add(new Event(
                    "1",
                    "Koncert i Havnen",
                    "Esbjerg Havn",
                    LocalDate.of(2025, 6, 12),
                    "En stor koncert ved havnen med lokale bands."
            ));

            events.add(new Event(
                    "2",
                    "Sommerfestival",
                    "Musikhuset",
                    LocalDate.of(2025, 7, 3),
                    "En hyggelig sommerfestival med madboder og musik."
            ));
        }
        eventTable.getItems().setAll(events);
    }

}
