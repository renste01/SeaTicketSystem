package dk.easv.seaticketsystem.Model;

// Java Imports
import java.util.UUID;

public class Admin extends User
{
    public Admin(String id, String firstName, String lastName, String email, String password)
    {
        super(id, firstName, lastName, email, password, UserRole.ADMIN);
    }

    public Admin(String firstName, String lastName, String email, String password)
    {
        this(UUID.randomUUID().toString(), firstName, lastName, email, password);
    }
}
