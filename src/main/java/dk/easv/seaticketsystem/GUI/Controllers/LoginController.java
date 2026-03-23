package dk.easv.seaticketsystem.GUI.Controllers;

// Projekt Imports
import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.MainApp;
import dk.easv.seaticketsystem.Session.SessionManager;
// Java Imports
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isBlank() || password.isBlank())
        {
            showError("Udfyld venligst alle felter.");
            return;
        }

        userService.authenticateStaff(email, password).ifPresentOrElse(user -> {
            SessionManager.getInstance().login(user);
            MainApp.setRoot("/dk/easv/seaticketsystem/Views/MainPageView.fxml", 1280, 800);
        }, () -> {
            showError("Login er kun for administratorer og eventkoordinatorer.");
            passwordField.clear();
            passwordField.requestFocus();
        });
    }

    @FXML
    private void handleKeyPress(KeyEvent e)
    {
        if (e.getCode() == KeyCode.ENTER) handleLogin();
    }

    private void showError(String msg)
    {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
