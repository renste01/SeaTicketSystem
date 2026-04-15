package dk.easv.seaticketsystem.GUI.Controllers;

// Projekt Imports
import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.BE.User;
import dk.easv.seaticketsystem.Model.UserSearchModel;
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
    @FXML private TilePane resultsContainer;
    @FXML private Label noResultsLabel;

    private final UserService userService = new UserService();
    private List<User> allUsers;
    private final UserSearchModel searchModel = new UserSearchModel();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        allUsers = userService.getAllUsers();
        searchField.textProperty().addListener((_, _, newVal) -> performSearch(newVal));
        performSearch("");
    }

    private void performSearch(String query) {
        resultsContainer.getChildren().clear();
        noResultsLabel.setVisible(false);
        noResultsLabel.setManaged(false);
        searchModel.setQuery(query);

        List<User> results;
        if (searchModel.getQuery().isEmpty()) {
            results = allUsers;
        } else {
            String lower = searchModel.getQuery().toLowerCase();
            results = allUsers.stream()
                    .filter(u ->
                            u.getName().toLowerCase().contains(lower) ||
                                    u.getEmail().toLowerCase().contains(lower))
                    .toList();
        }

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
        card.getStyleClass().add("user-search-card");
        card.setPadding(new Insets(20));

        // Avatar
        String initials = "";
        if (user.getFirstName() != null && !user.getFirstName().isEmpty())
            initials += user.getFirstName().charAt(0);
        if (user.getLastName() != null && !user.getLastName().isEmpty())
            initials += user.getLastName().charAt(0);

        Label avatar = new Label(initials.toUpperCase());
        avatar.getStyleClass().add("user-search-avatar");

        // Name and role
        Label nameLabel = new Label(user.getName());
        nameLabel.getStyleClass().add("user-search-name");

        Label roleLabel = new Label(user.getRole().getDisplayName());
        roleLabel.getStyleClass().add("user-search-role");

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
        viewBtn.getStyleClass().add("user-search-btn");
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
        keyLabel.getStyleClass().add("user-search-detail-key");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("user-search-detail-value");
        valueLabel.setWrapText(true);

        HBox row = new HBox(10, keyLabel, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}

