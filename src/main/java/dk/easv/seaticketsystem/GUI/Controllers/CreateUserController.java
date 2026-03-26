package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.Admin;
import dk.easv.seaticketsystem.Model.EventCoordinator;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateUserController implements Initializable {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleComboBox.getItems().addAll(UserRole.ADMIN, UserRole.COORDINATOR);
    }

    @FXML
    private void handleCreate() {
        String fn = firstNameField.getText().trim();
        String ln = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String pw = passwordField.getText();
        UserRole role = roleComboBox.getValue();

        if (fn.isEmpty() || ln.isEmpty() || email.isEmpty() || pw.isEmpty() || role == null) {
            showError("Udfyld alle felter og vælg en rolle.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError("E-mailen ikke gyldig.");
            return;
        }
        if (pw.length() < 4) {
            showError("Adgangskoden skal mindst være 4 tegn.");
            return;
        }

        User newUser = (role == UserRole.ADMIN)
                ? new Admin(fn, ln, email, pw)
                : new EventCoordinator(fn, ln, email, pw);

        userService.createUserTest(newUser);
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}