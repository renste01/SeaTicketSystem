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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // ViewManager skal kende contentArea
        ViewManager.getInstance().setContentArea(contentArea);

        // Logo
        Pane logo = LogoUtil.create(175, Color.WHITE);
        sidebarLogoPane.getChildren().setAll(logo);

        // Hent bruger
        User user = SessionManager.getInstance().getCurrentUser();
        userNameLabel.setText(user.getName());
        userRoleLabel.setText(user.getRole().getDisplayName());

        // Skjul begge menuer som udgangspunkt
        setVisible(coordinatorMenu, false);
        setVisible(adminMenu, false);

        // Vis menu baseret på rolle
        UserRole role = user.getRole();
        if (role == UserRole.COORDINATOR) setVisible(coordinatorMenu, true);
        if (role == UserRole.ADMIN) setVisible(adminMenu, true);

        // Standard tekst i midten
        contentArea.getChildren().setAll(new Label("Homepage klar. Vælg en menu-side."));
    }

    private void setVisible(VBox node, boolean v) {
        node.setVisible(v);
        node.setManaged(v);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        MainApp.setRoot("/dk/easv/seaticketsystem/Views/LoginView.fxml", 1280, 800);
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

}
