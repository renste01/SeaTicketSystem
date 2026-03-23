package dk.easv.seaticketsystem.DAL;

// Project Imports
import dk.easv.seaticketsystem.Model.Admin;
import dk.easv.seaticketsystem.Model.EventCoordinator;
import dk.easv.seaticketsystem.Model.User;

// Java Imports
import java.util.Optional;
import java.sql.*;
import java.io.IOException;

public class UserRepository
{
    public Optional<User> findStaffByEmail(String email)
    {
       String sql =
               """
               SELECT UserId, FirstName, LastName, Email, [Password], UserRole
               From dbo.Users
               WHERE Email = ? AND IsActive = 1 AND UserRole IN ('Admin', 'COORDINATOR')
               """;
       try
       {
           DBConnector.getInstance();
           try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)){

               ps.setString(1, email);
               try (ResultSet rs = ps.executeQuery())
               {
                   if (!rs.next()) return Optional.empty();
                   return Optional.of(mapUser(rs));
               }
           }
       } catch (SQLException | IOException e){
           e.printStackTrace();
           return Optional.empty();
       }
    }
    private User mapUser (ResultSet rs) throws SQLException{
        String id = rs.getString("UserId");
        String first = rs.getString("Firstname");
        String last = rs.getString("Lastname");
        String email = rs.getString("Email");
        String password = rs.getString("Password");
        String role = rs.getString("UserRole");

        if ("ADMIN".equalsIgnoreCase(role))
        {
            return new Admin(id, first, last, email, password);
        }
        return new EventCoordinator(id, first, last, email, password);
    }
}
