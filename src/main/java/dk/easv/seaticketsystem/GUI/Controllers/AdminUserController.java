package dk.easv.seaticketsystem.GUI.Controllers;

// Projekt Imports
import dk.easv.seaticketsystem.BLL.UserService;
import dk.easv.seaticketsystem.BE.User;

// Java Imports
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminUserController implements Initializable {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().getDisplayName()));

        refreshTable();
    }

    private void refreshTable() {
        userTable.getItems().setAll(userService.getAllUsers());
    }

    @FXML
    private void handleOpenCreateUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/dk/easv/seaticketsystem/Views/CreateUserView.fxml"));
            Stage popup = new Stage();
            popup.setTitle("Opret bruger");
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setScene(new Scene(loader.load()));
            popup.showAndWait();
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        userService.deleteUser(selected.getId());
        refreshTable();
    }
}