package dk.easv.seaticketsystem.BLL;
// Project Imports
import dk.easv.seaticketsystem.Model.Admin;
import dk.easv.seaticketsystem.Model.EventCoordinator;
import dk.easv.seaticketsystem.Model.User;
import dk.easv.seaticketsystem.Model.UserRole;

// Java Imports
import java.util.List;
import java.util.Optional;

public class UserService
{

    private final List<User> staffUsers = List.of(new Admin("admin-001", "System", "Administrator", "admin@sea.dk", "admin123"), new EventCoordinator("coord-001", "Lars", "Nielsen", "koordinator@sea.dk", "koor123"));

    public Optional<User> authenticateStaff(String email, String password)
    {
        return staffUsers.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .filter(u -> u.checkPassword(password))
                .filter(u -> u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.COORDINATOR)
                .findFirst();
    }
}
