package dk.easv.seaticketsystem.GUI.Controllers;

// Projekt Imports
import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;

// Java Imports
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserSearchController implements Initializable {

    @FXML private TextField searchField;
    @FXML private VBox resultsContainer;
    @FXML private Label noResultsLabel;

    private final UserService userService = new UserService();
    private List<User> allUsers;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        allUsers = userService.getAllUsers();
        searchField.textProperty().addListener((_, _, newVal) -> performSearch(newVal));
    }

    private void performSearch(String query) {
        resultsContainer.getChildren().clear();
        noResultsLabel.setVisible(false);
        noResultsLabel.setManaged(false);

        if (query == null || query.trim().isEmpty()) return;

        String lower = query.trim().toLowerCase();

        List<User> results = allUsers.stream()
                .filter(u ->
                        u.getName().toLowerCase().contains(lower) ||
                                u.getEmail().toLowerCase().contains(lower))
                .toList();

        if (results.isEmpty()) {
            noResultsLabel.setVisible(true);
            noResultsLabel.setManaged(true);
            return;
        }

        for (User user : results) {
            resultsContainer.getChildren().add(buildUserCard(user));
        }
    }

    private VBox buildUserCard(User user) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #e4e8ec;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 2);"
        );
        card.setPadding(new Insets(20));

        // Avatar
        String initials = "";
        if (user.getFirstName() != null && !user.getFirstName().isEmpty())
            initials += user.getFirstName().charAt(0);
        if (user.getLastName() != null && !user.getLastName().isEmpty())
            initials += user.getLastName().charAt(0);

        Label avatar = new Label(initials.toUpperCase());
        avatar.setStyle(
                "-fx-background-color: #002430;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-min-width: 52px;" +
                        "-fx-min-height: 52px;" +
                        "-fx-max-width: 52px;" +
                        "-fx-max-height: 52px;" +
                        "-fx-background-radius: 26;" +
                        "-fx-alignment: center;"
        );

        // Name and role
        Label nameLabel = new Label(user.getName());
        nameLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #002430;");

        Label roleLabel = new Label(user.getRole().getDisplayName());
        roleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #db3629;");

        VBox nameBox = new VBox(3, nameLabel, roleLabel);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        HBox headerRow = new HBox(14, avatar, nameBox);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator();

        // Detail rows
        HBox emailRow = detailRow("✉️  Email:", user.getEmail());
        HBox roleRow  = detailRow("👤  Rolle:", user.getRole().getDisplayName());

        // View profile button
        Button viewBtn = new Button("👁  Se profil");
        viewBtn.setStyle(
                "-fx-background-color: #002430;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 8 20 8 20;" +
                        "-fx-cursor: hand;"
        );
        viewBtn.setOnAction(_ -> {
            UserProfileViewController.setUser(user);
            ViewManager.getInstance().loadView("UserProfileView.fxml");
        });

        HBox buttonRow = new HBox(viewBtn);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(headerRow, sep, emailRow, roleRow, buttonRow);
        return card;
    }

    private HBox detailRow(String key, String value) {
        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #888; -fx-min-width: 100px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #002430;");
        valueLabel.setWrapText(true);

        HBox row = new HBox(10, keyLabel, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}