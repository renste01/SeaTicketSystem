package dk.easv.seaticketsystem.BLL;

import dk.easv.seaticketsystem.Model.Admin;
import dk.easv.seaticketsystem.Model.EventCoordinator;
import dk.easv.seaticketsystem.Model.User;

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
                .findFirst();
    }

    public User createUser(User user) {
        staffUsers.add(user);
        return user;
    }

    public boolean deleteUser(String userId) {
        return staffUsers.removeIf(u -> u.getId().equals(userId));
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(staffUsers);
    }
}