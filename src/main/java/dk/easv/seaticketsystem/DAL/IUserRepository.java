package dk.easv.seaticketsystem.DAL;

// Projekt Imports
import dk.easv.seaticketsystem.Model.User;

// Java Imports
import java.util.List;
import java.util.Optional;

public interface IUserRepository
{
    // Find
    Optional<User> findStaffByEmail(String email);
    Optional<User> findRegularUserByEmail(String email);

    // Read all
    List<User> getAllUsers();

    // Create
    void createUser(User user) throws Exception;
    User createTicketUser(String fullName, String email);

    // Update/Delete
    void updateUser(User user);
    void deleteUser(String userId);
}

