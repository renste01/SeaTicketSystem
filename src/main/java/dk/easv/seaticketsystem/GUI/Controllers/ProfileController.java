package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private Label roleLabel;
    @FXML private Label infoFeedbackLabel;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordFeedbackLabel;

    private final UserService userService = new UserService();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            System.out.println("ERROR: currentUser is null in ProfileController!");
            return;
        }

        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());

        // Safely set role label — falls back to role name if getDisplayName() doesn't exist
        try {
            roleLabel.setText(currentUser.getRole().getDisplayName());
        } catch (Exception e) {
            roleLabel.setText(currentUser.getRole().name());
            System.out.println("WARNING: getDisplayName() failed, falling back to role.name(): " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveInfo() {
        if (currentUser == null) {
            showFeedback(infoFeedbackLabel, "Ingen bruger er logget ind.", false);
            return;
        }

        String fn = firstNameField.getText().trim();
        String ln = lastNameField.getText().trim();
        String email = emailField.getText().trim();

        if (fn.isEmpty() || ln.isEmpty() || email.isEmpty()) {
            showFeedback(infoFeedbackLabel, "Udfyld alle felter.", false);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showFeedback(infoFeedbackLabel, "E-mailen er ikke gyldig.", false);
            return;
        }

        currentUser.setFirstName(fn);
        currentUser.setLastName(ln);
        currentUser.setEmail(email);

        try {
            userService.updateUser(currentUser);
            showFeedback(infoFeedbackLabel, "✔ Oplysninger gemt!", true);
        } catch (Exception e) {
            showFeedback(infoFeedbackLabel, "Fejl: kunne ikke gemme oplysninger.", false);
            System.out.println("ERROR saving user info: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePassword() {
        if (currentUser == null) {
            showFeedback(passwordFeedbackLabel, "Ingen bruger er logget ind.", false);
            return;
        }

        String current = currentPasswordField.getText();
        String newPw = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (current.isEmpty() || newPw.isEmpty() || confirm.isEmpty()) {
            showFeedback(passwordFeedbackLabel, "Udfyld alle felter.", false);
            return;
        }
        if (!currentUser.checkPassword(current)) {
            showFeedback(passwordFeedbackLabel, "Nuværende adgangskode er forkert.", false);
            return;
        }
        if (newPw.length() < 4) {
            showFeedback(passwordFeedbackLabel, "Ny adgangskode skal være mindst 4 tegn.", false);
            return;
        }
        if (!newPw.equals(confirm)) {
            showFeedback(passwordFeedbackLabel, "Adgangskoderne matcher ikke.", false);
            return;
        }

        currentUser.setPassword(newPw);

        try {
            userService.updateUser(currentUser);
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            showFeedback(passwordFeedbackLabel, "✔ Adgangskode ændret!", true);
        } catch (Exception e) {
            showFeedback(passwordFeedbackLabel, "Fejl: kunne ikke ændre adgangskode.", false);
            System.out.println("ERROR changing password: " + e.getMessage());
        }
    }

    private void showFeedback(Label label, String msg, boolean success) {
        label.setText(msg);
        label.setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        label.setVisible(true);
        label.setManaged(true);
    }
}