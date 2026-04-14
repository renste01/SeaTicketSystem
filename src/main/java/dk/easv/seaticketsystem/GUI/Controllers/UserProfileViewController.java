package dk.easv.seaticketsystem.GUI.Controllers;

// Projekt Imports
import dk.easv.seaticketsystem.BE.User;
import dk.easv.seaticketsystem.GUI.Util.ViewManager;

// Java Imports
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;

public class UserProfileViewController implements Initializable {

    @FXML private Label initialsLabel;
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Label emailLabel;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;

    private static User selectedUser;

    public static void setUser(User user) {
        selectedUser = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (selectedUser == null) return;

        // Avatar initials
        String initials = "";
        if (selectedUser.getFirstName() != null && !selectedUser.getFirstName().isEmpty())
            initials += selectedUser.getFirstName().charAt(0);
        if (selectedUser.getLastName() != null && !selectedUser.getLastName().isEmpty())
            initials += selectedUser.getLastName().charAt(0);
        initialsLabel.setText(initials.toUpperCase());

        nameLabel.setText(selectedUser.getName());
        roleLabel.setText(selectedUser.getRole().getDisplayName());
        emailLabel.setText(selectedUser.getEmail());
        firstNameLabel.setText(selectedUser.getFirstName());
        lastNameLabel.setText(selectedUser.getLastName());
    }

    @FXML
    private void handleBack() {
        ViewManager.getInstance().loadView("UserSearchView.fxml");
    }
}
