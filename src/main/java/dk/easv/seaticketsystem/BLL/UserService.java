package dk.easv.seaticketsystem.BLL;

import dk.easv.seaticketsystem.DAL.UserRepository;
import dk.easv.seaticketsystem.DAL.DBConnector;
import dk.easv.seaticketsystem.Model.Admin;
import dk.easv.seaticketsystem.Model.EventCoordinator;
import dk.easv.seaticketsystem.Model.User;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userDAO = new UserRepository();

    // Offline fallback-brugere
    private final List<User> offlineUsers = new ArrayList<>(List.of(
            new Admin("admin-001", "System", "Administrator", "admin@sea.dk", "admin123"),
            new EventCoordinator("coord-001", "Lars", "Nielsen", "koordinator@sea.dk", "koor123")
    ));

    // Automatisk check: virker databasen?
    private boolean databaseAvailable() {
        try (Connection c = DBConnector.getInstance().getConnection()) {
            return c != null && !c.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    // LOGIN
    public Optional<User> authenticateStaff(String email, String password) {

        if (!databaseAvailable()) {
            // Offline mode
            return offlineUsers.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .filter(u -> u.checkPassword(password))
                    .findFirst();
        }

        // Online mode
        return userDAO.findStaffByEmail(email)
                .filter(u -> u.checkPassword(password));
    }

    // GET ALL USERS
    public List<User> getAllUsers() {
        if (!databaseAvailable()) {
            return new ArrayList<>(offlineUsers);
        }
        return userDAO.getAllUsers();
    }

    // CREATE USER
    public void createUser(User newUser) {
        if (!databaseAvailable()) {
            offlineUsers.add(newUser);
            return;
        }

        try {
            userDAO.createUser(newUser);
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke oprette bruger i databasen", e);
        }
    }

    // UPDATE USER
    public void updateUser(User user) {
        if (!databaseAvailable()) {
            return; // offline mode gør ingenting
        }

        try {
            userDAO.updateUser(user);
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke opdatere bruger", e);
        }
    }

    // DELETE USER
    public void deleteUser(String userId) {
        if (!databaseAvailable()) {
            offlineUsers.removeIf(u -> u.getId().equals(userId));
            return;
        }

        try {
            userDAO.deleteUser(userId);
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke slette bruger", e);
        }
    }
}