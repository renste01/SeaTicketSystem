package dk.easv.seaticketsystem.BLL;

import dk.easv.seaticketsystem.Model.Admin;
import dk.easv.seaticketsystem.Model.EventCoordinator;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final List<User> staffUsers = new ArrayList<>(List.of(
            new Admin("admin-001", "System", "Administrator", "admin@sea.dk", "admin123"),
            new EventCoordinator("coord-001", "Lars", "Nielsen", "koordinator@sea.dk", "koor123")
    ));

    public Optional<User> authenticateStaff(String email, String password) {
        return staffUsers.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .filter(u -> u.checkPassword(password))
                .filter(u -> u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.COORDINATOR)
                .findFirst();
    }

    public void createUser(User newUser) {
        boolean exists = staffUsers.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(newUser.getEmail()));

        if (exists) {
            throw new IllegalArgumentException("En bruger med denne email findes allerede.");
        }

        staffUsers.add(newUser);
    }

    public List<User> getAllUsers() {
        return List.copyOf(staffUsers);
    }

    public void deleteUser(String id) {
        boolean removed = staffUsers.removeIf(u -> u.getId().equalsIgnoreCase(id));

        if (!removed) {
            throw new IllegalArgumentException("Ingen bruger med ID: " + id);
        }
    }
}