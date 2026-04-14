package dk.easv.seaticketsystem.BE;

// Java Imports
import java.util.UUID;

public class RegularUser extends User
{
    public RegularUser(String id, String name, String email)
    {
        super(id, name, "", email, null, UserRole.USER);
    }

    public RegularUser(String name, String email)
    {
        this(UUID.randomUUID().toString(), name, email);
    }
}

