package dk.easv.seaticketsystem.GUI.Controllers;

// Project Imports
import dk.easv.seaticketsystem.MainApp;
import dk.easv.seaticketsystem.GUI.Util.LogoUtil;
import dk.easv.seaticketsystem.BE.User;
import dk.easv.seaticketsystem.BE.UserRole;
import dk.easv.seaticketsystem.Session.SessionManager;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;

// Java Imports
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
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
    @FXML private VBox userMenu;
    @FXML private StackPane sidebarLogoPane;

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

        // Show menus based on role
        UserRole role = user.getRole();
        if (role == UserRole.COORDINATOR) {
            setVisible(coordinatorMenu, true);
            setVisible(userMenu, true);
        }
        if (role == UserRole.ADMIN) {
            setVisible(adminMenu, true);
            setVisible(userMenu, true);
        }

        openHomepage();
    }

    private void setVisible(VBox node, boolean v) {
        node.setVisible(v);
        node.setManaged(v);
    }

    @FXML
    private void openHomepage() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page-compact");
        page.setPadding(new Insets(18, 20, 18, 20));

        Label title = new Label("Homepage");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Vælg en menu direkte herfra.");
        subtitle.getStyleClass().add("info-label");

        TilePane tiles = new TilePane();
        tiles.setPrefColumns(2);
        tiles.setHgap(16);
        tiles.setVgap(16);

        tiles.getChildren().addAll(
                createHomeTile("🎟", "Mine Billetter", this::openMyTickets),
                createHomeTile("📅", "Events", this::openEvents),
                createHomeTile("👤", "Min Profil", this::handleProfile)
        );

        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            if (user.getRole() == UserRole.COORDINATOR || user.getRole() == UserRole.ADMIN) {
                tiles.getChildren().add(createHomeTile("🔍", "Søg Bruger", this::openUserSearch));
            }
            if (user.getRole() == UserRole.COORDINATOR) {
                tiles.getChildren().addAll(
                        createHomeTile("📋", "Mine Events", this::openMyEvents),
                        createHomeTile("➕", "Opret Event", this::openCreateEvent),
                        createHomeTile("🎁", "Fribilletter", this::openFreeTickets)
                );
            }
            if (user.getRole() == UserRole.ADMIN) {
                tiles.getChildren().addAll(
                        createHomeTile("📊", "Dashboard", this::openDashboard),
                        createHomeTile("👥", "Administrer Brugere", this::openUserAdmin),
                        createHomeTile("📂", "Administrer Events", this::openAdminEvents),
                        createHomeTile("🗃", "Slettede data", this::openDeletedItems)
                );
            }
        }

        page.getChildren().addAll(title, subtitle, tiles);
        contentArea.getChildren().setAll(page);
    }

    private Button createHomeTile(String icon, String text, Runnable onClick) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("home-tile-icon");

        Button tile = new Button(text);
        tile.getStyleClass().add("home-tile");
        tile.setGraphic(iconLabel);
        tile.setContentDisplay(ContentDisplay.TOP);
        tile.setGraphicTextGap(10);
        tile.setWrapText(true);
        tile.setAlignment(Pos.CENTER);
        tile.setOnAction(e -> onClick.run());
        return tile;
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
        EventFormController.clearEventToEdit();
        ViewManager.getInstance().loadView("EventFormView.fxml");
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
    private void openDeletedItems() {
        ViewManager.getInstance().loadView("AdminDeletedItemsView.fxml");
    }

    @FXML
    private void openMyEvents() {
        ViewManager.getInstance().loadView("MyEventsView.fxml");
    }

    @FXML
    private void openUserSearch() {
        ViewManager.getInstance().loadView("UserSearchView.fxml");
    }

    @FXML
    private void openFreeTickets() {
        MyTicketsController controller =
                ViewManager.getInstance().navigateToWithController("MyTicketsView");

        controller.setShowOnlyFreeTickets(true);
    }
}
