package dk.easv.seaticketsystem.GUI.Controllers;

import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Session.SessionManager;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ProfileController implements Initializable {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private Button saveInfoButton;
    @FXML private Label roleLabel;
    @FXML private Label infoFeedbackLabel;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label passwordFeedbackLabel;

    private final UserService userService = new UserService();
    private User currentUser;
    private String originalFirstName = "";
    private String originalLastName = "";
    private String originalEmail = "";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÆØÅæøå\\- ]{2,}$");

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
        originalFirstName = currentUser.getFirstName();
        originalLastName = currentUser.getLastName();
        originalEmail = currentUser.getEmail();

        // Safely set role label — falls back to role name if getDisplayName() doesn't exist
        try {
            roleLabel.setText(currentUser.getRole().getDisplayName());
        } catch (Exception e) {
            roleLabel.setText(currentUser.getRole().name());
            System.out.println("WARNING: getDisplayName() failed, falling back to role.name(): " + e.getMessage());
        }

        setupInfoChangeTracking();
        hideFeedback(infoFeedbackLabel);
        hideFeedback(passwordFeedbackLabel);
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
        if (!NAME_PATTERN.matcher(fn).matches() || !NAME_PATTERN.matcher(ln).matches()) {
            showFeedback(infoFeedbackLabel, "Navn skal være mindst 2 bogstaver.", false);
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showFeedback(infoFeedbackLabel, "E-mailen er ikke gyldig.", false);
            return;
        }

        currentUser.setFirstName(fn);
        currentUser.setLastName(ln);
        currentUser.setEmail(email);

        try {
            userService.updateUser(currentUser);
            originalFirstName = fn;
            originalLastName = ln;
            originalEmail = email;
            updateSaveButtonState();
            showFeedback(infoFeedbackLabel, "✔ Oplysninger gemt!", true);
        } catch (RuntimeException e) {
            if (isDuplicateEmailError(e)) {
                showFeedback(infoFeedbackLabel, "E-mail findes allerede.", false);
                return;
            }
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
        if (newPw.length() < 8) {
            showFeedback(passwordFeedbackLabel, "Ny adgangskode skal være mindst 8 tegn.", false);
            return;
        }
        if (!containsLetter(newPw) || !containsDigit(newPw)) {
            showFeedback(passwordFeedbackLabel, "Adgangskode skal indeholde baade bogstav og tal.", false);
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
        label.getStyleClass().removeAll("feedback-success", "feedback-error");
        label.getStyleClass().add(success ? "feedback-success" : "feedback-error");
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideFeedback(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    private void setupInfoChangeTracking() {
        ChangeListener<String> listener = (obs, oldValue, newValue) -> {
            hideFeedback(infoFeedbackLabel);
            updateSaveButtonState();
        };

        firstNameField.textProperty().addListener(listener);
        lastNameField.textProperty().addListener(listener);
        emailField.textProperty().addListener(listener);
        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        if (saveInfoButton == null) return;
        saveInfoButton.setDisable(!hasInfoChanges());
    }

    private boolean hasInfoChanges() {
        String fn = firstNameField.getText() == null ? "" : firstNameField.getText().trim();
        String ln = lastNameField.getText() == null ? "" : lastNameField.getText().trim();
        String em = emailField.getText() == null ? "" : emailField.getText().trim();
        return !fn.equals(originalFirstName) || !ln.equals(originalLastName) || !em.equals(originalEmail);
    }

    private boolean containsLetter(String text) {
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) return true;
        }
        return false;
    }

    private boolean containsDigit(String text) {
        for (char c : text.toCharArray()) {
            if (Character.isDigit(c)) return true;
        }
        return false;
    }

    private boolean isDuplicateEmailError(RuntimeException e) {
        String message = e.getMessage();
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.contains("unique") || lower.contains("duplicate") || lower.contains("already exists");
    }
}
