package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.Model.Event;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.time.LocalDate;
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

        loadDummyEvents();
    }

    private void loadDummyEvents() {
        List<Event> dummy = List.of(
                new Event("1", "Koncert i Havnen", "Esbjerg Havn", LocalDate.of(2025, 6, 12)),
                new Event("2", "Sommerfestival", "Musikhuset", LocalDate.of(2025, 7, 3))
        );

        eventTable.getItems().setAll(dummy);
    }
}
