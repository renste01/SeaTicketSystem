package dk.easv.seaticketsystem.Model;

import java.util.UUID;

public class RegularUser extends User
{
    public RegularUser(String id, String firstName, String lastName, String email, String
            password)
    {
        super(id, firstName, lastName, email, password, UserRole.USER);
    }

    public RegularUser(String firstName, String lastName, String email, String password)
    {
        this(UUID.randomUUID().toString(), firstName, lastName, email, password);
    }
}
