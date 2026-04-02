package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.Event;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label totalUsersLabel;
    @FXML private Label totalCoordinatorsLabel;
    @FXML private Label totalAdminsLabel;
    @FXML private Label totalEventsLabel;
    @FXML private Label welcomeLabel;

    @FXML private TableView<User> recentUsersTable;
    @FXML private TableColumn<User, String> colUserName;
    @FXML private TableColumn<User, String> colUserEmail;
    @FXML private TableColumn<User, String> colUserRole;

    @FXML private TableView<Event> recentEventsTable;
    @FXML private TableColumn<Event, String> colEventTitle;
    @FXML private TableColumn<Event, String> colEventDate;
    @FXML private TableColumn<Event, String> colEventLocation;
    @FXML private TableColumn<Event, String> colEventCoordinators;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null) {
            welcomeLabel.setText("Velkommen tilbage, " + current.getFirstName() + " 👋");
        }

        colUserName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colUserEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colUserRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().getDisplayName()));

        colEventTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colEventDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateRangeDisplay()));
        colEventLocation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));
        colEventCoordinators.setCellValueFactory(c -> new SimpleStringProperty(getCoordinatorNames(c.getValue())));

        loadData();
    }

    private void loadData() {
        List<User> allUsers;
        try {
            allUsers = userService.getAllUsers();
            System.out.println("Dashboard loaded " + allUsers.size() + " users");
        } catch (Exception e) {
            System.out.println("ERROR loading users: " + e.getMessage());
            allUsers = new ArrayList<>();
        }

        List<Event> allEvents = EventListController.getEvents();
        System.out.println("Dashboard loaded " + allEvents.size() + " events");

        long coordinators = allUsers.stream().filter(u -> u.getRole() == UserRole.COORDINATOR).count();
        long admins = allUsers.stream().filter(u -> u.getRole() == UserRole.ADMIN).count();

        totalUsersLabel.setText(String.valueOf(allUsers.size()));
        totalCoordinatorsLabel.setText(String.valueOf(coordinators));
        totalAdminsLabel.setText(String.valueOf(admins));
        totalEventsLabel.setText(String.valueOf(allEvents.size()));

        recentUsersTable.getItems().setAll(allUsers);
        recentEventsTable.getItems().setAll(allEvents);
    }

    private String getCoordinatorNames(Event event) {
        List<User> allUsers = userService.getAllUsers();
        List<String> names = new ArrayList<>();

        String ownerId = event.getOwnerCoordinatorId();
        if (ownerId != null) {
            for (User user : allUsers) {
                if (user.getId().equals(ownerId)) {
                    names.add(user.getName());
                    break;
                }
            }
        }

        for (String coId : event.getCoCoordinatorIds()) {
            for (User user : allUsers) {
                if (user.getId().equals(coId)) {
                    names.add(user.getName());
                    break;
                }
            }
        }

        if (names.isEmpty()) return "Ingen";
        return String.join(", ", names);
    }
}
