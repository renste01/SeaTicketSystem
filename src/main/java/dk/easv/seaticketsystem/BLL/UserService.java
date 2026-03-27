package dk.easv.seaticketsystem.BLL;

import dk.easv.seaticketsystem.DAL.UserRepository;
import dk.easv.seaticketsystem.Model.Admin;
import dk.easv.seaticketsystem.Model.EventCoordinator;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userDAO = new UserRepository();

    private final List<User> staffUsers = new ArrayList<>(List.of(
            new Admin("admin-001", "System", "Administrator", "admin@sea.dk", "admin123"),
            new EventCoordinator("coord-001", "Lars", "Nielsen", "koordinator@sea.dk", "koor123")
    ));

// til ahrd coding
   /* public Optional<User> authenticateStaff(String email, String password) {
        return staffUsers.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .filter(u -> u.checkPassword(password))
                .filter(u -> u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.COORDINATOR)
                .findFirst();
    }
*/
    public Optional<User> authenticateStaff(String email, String password){

        return userDAO.findStaffByEmail(email).filter(u -> u.checkPassword(password));
    }


    public void createUserTest(User newUser) {
        boolean exists = staffUsers.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(newUser.getEmail()));

        if (exists) {
            throw new IllegalArgumentException("En bruger med denne email findes allerede.");
        }

        staffUsers.add(newUser);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public void createUser(User newUser) {
        try {
            userDAO.createUser(newUser);
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke oprette bruger i databasen", e);
        }
    }

    public void updateUser(User user) {
        try {
            userDAO.updateUser(user);
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke opdatere bruger", e);
        }
    }

    public void deleteUser(String userId) {
        try {
            userDAO.deleteUser(userId);
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke slette bruger", e);
        }
    }


}