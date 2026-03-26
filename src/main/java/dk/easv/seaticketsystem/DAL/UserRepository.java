package dk.easv.seaticketsystem.DAL;

// Project Imports
import dk.easv.seaticketsystem.Model.Admin;
import dk.easv.seaticketsystem.Model.EventCoordinator;
import dk.easv.seaticketsystem.Model.User;

// Java Imports
import java.util.ArrayList;
import java.util.List;
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
               WHERE Email = ? AND UserRole IN ('Admin', 'COORDINATOR')
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

    public void createUser(User user)throws Exception{
        String sql = "INSERT INTO Users (UserId, FirstName, LastName, Email, Password, UserRole) Values(?,?,?,?,?,?)";
         try (Connection conn = DBConnector.getInstance().getConnection();
         PreparedStatement stmt = (conn.prepareStatement(sql))){

             stmt.setString(1, user.getId());
             stmt.setString(2, user.getFirstName());
             stmt.setString(3, user.getLastName());
             stmt.setString(4, user.getEmail());
             stmt.setString(5, user.getPassword());
             stmt.setString(6, user.getRole().name());

             stmt.executeUpdate();
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

    public List<User> getAllUsers() {
        String sql = "SELECT UserId, FirstName, LastName, Email, Password, UserRole FROM Users";

        List<User> users = new ArrayList<>();

        try (Connection con = DBConnector.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public void deleteUser(String userId) {
        String sql = "DELETE FROM Users WHERE UserId = ?";

        try (Connection con = DBConnector.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


