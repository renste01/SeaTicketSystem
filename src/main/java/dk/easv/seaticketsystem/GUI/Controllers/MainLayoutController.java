package dk.easv.seaticketsystem.GUI.Controllers;

// Projekt Imports
import dk.easv.seaticketsystem.MainApp;
import dk.easv.seaticketsystem.GUI.Util.LogoUtil;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;

// Java Imports
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private VBox coordinatorMenu;
    @FXML private VBox adminMenu;
    @FXML private StackPane sidebarLogoPane;
    @FXML private VBox userMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewManager.getInstance().setContentArea(contentArea);

        Pane logo = LogoUtil.create(175, Color.WHITE);
        sidebarLogoPane.getChildren().setAll(logo);

        User user = SessionManager.getInstance().getCurrentUser();
        userNameLabel.setText(user.getName());
        userRoleLabel.setText(user.getRole().getDisplayName());

        // Hide all role menus by default
        setVisible(coordinatorMenu, false);
        setVisible(adminMenu, false);
        setVisible(userMenu, false);

        // Show menu based on role
        UserRole role = user.getRole();
        if (role == UserRole.COORDINATOR) setVisible(coordinatorMenu, true);
        if (role == UserRole.ADMIN)       setVisible(adminMenu, true);
        if (role == UserRole.USER)        setVisible(userMenu, true);

        contentArea.getChildren().setAll(new Label("Homepage klar. Vælg en menu-side."));
    }

    private void setVisible(VBox node, boolean v) {
        node.setVisible(v);
        node.setManaged(v);
    }
    @FXML
    private void openMyTickets() {
        ViewManager.getInstance().loadView("MyTicketsView.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        MainApp.setRoot("/dk/easv/seaticketsystem/Views/LoginView.fxml", 1280, 800);
    }

    @FXML
    private void handleProfile() {
        ViewManager.getInstance().loadView("ProfileView.fxml");
    }

    @FXML
    private void openUserAdmin() {
        ViewManager.getInstance().loadView("AdminUserView.fxml");
    }

    @FXML
    private void openEvents() {
        ViewManager.getInstance().loadView("EventListView.fxml");
    }

    @FXML
    private void openCreateEvent() {
        ViewManager.getInstance().loadView("CreateEventView.fxml");
    }

    @FXML
    private void openDashboard() {
        ViewManager.getInstance().loadView("AdminDashboardView.fxml");
    }
    @FXML
    private void openAdminEvents() {
        ViewManager.getInstance().loadView("AdminEventManager.fxml");
    }
    @FXML
    private void openMyEvents() {
        ViewManager.getInstance().loadView("MyEventsView.fxml");
    }
    @FXML
    private void openUserSearch() {
        ViewManager.getInstance().loadView("UserSearchView.fxml");
    }
}