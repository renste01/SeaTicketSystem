package dk.easv.seaticketsystem.DAL;

// Projekt Imports
import dk.easv.seaticketsystem.BE.Admin;
import dk.easv.seaticketsystem.BE.EventCoordinator;
import dk.easv.seaticketsystem.BE.RegularUser;
import dk.easv.seaticketsystem.BE.User;
import dk.easv.seaticketsystem.BE.UserRole;

// Java Imports
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;
import java.io.IOException;

public class UserRepository implements IUserRepository
{
    public Optional<User> findStaffByEmail(String email)
    {
        String sql =
                """
                SELECT UserId, FirstName, LastName, Email, [Password], UserRole
                From dbo.Users
                WHERE Email = ? AND UserRole IN ('ADMIN', 'COORDINATOR') AND IsDeleted = 0
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

    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO Users (UserId, FirstName, LastName, Email, Password, UserRole, IsDeleted) Values(?,?,?,?,?,?,?)";
        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getId());
            stmt.setString(2, user.getFirstName());
            stmt.setString(3, user.getLastName());
            stmt.setString(4, user.getEmail());
            if (user.getRole() == UserRole.USER) {
                stmt.setNull(5, Types.NVARCHAR);
            } else {
                stmt.setString(5, user.getPassword());
            }
            stmt.setString(6, user.getRole().name());
            stmt.setBoolean(7, false);

            stmt.executeUpdate();
        } catch (IOException e) {
            throw new SQLException("Kunne ikke oprette bruger pga. konfigurationsfejl", e);
        }
    }

    public Optional<User> findRegularUserByEmail(String email) {
        String sql =
                """
                SELECT UserId, FirstName, LastName, Email, [Password], UserRole
                FROM dbo.Users
                WHERE Email = ? AND UserRole = 'USER' AND IsDeleted = 0
                """;
        try (Connection con = DBConnector.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapUser(rs));
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Kunne ikke hente billetbruger", e);
        }
    }

    public User createTicketUser(String fullName, String email) {
        String sql = "INSERT INTO Users (UserId, FirstName, LastName, Email, Password, UserRole, IsDeleted) VALUES (?,?,?,?,?,?,?)";
        String userId = java.util.UUID.randomUUID().toString();
        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, fullName);
            stmt.setNull(3, Types.NVARCHAR);
            stmt.setString(4, email);
            stmt.setNull(5, Types.NVARCHAR);
            stmt.setString(6, "USER");
            stmt.setBoolean(7, false);
            stmt.executeUpdate();
            return new RegularUser(userId, fullName, email);
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Kunne ikke oprette billetbruger", e);
        }
    }

    public void updateUser(User user) {
        String sql = "UPDATE Users SET FirstName = ?, LastName = ?, Email = ?, Password = ? WHERE UserId = ?";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getId());

            ps.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new RuntimeException("Could not update user in database: " + e.getMessage(), e);
        }
    }

    public List<User> getAllUsers() {
        String sql = "SELECT UserId, FirstName, LastName, Email, Password, UserRole FROM Users WHERE IsDeleted = 0";

        List<User> users = new ArrayList<>();

        try (Connection con = DBConnector.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return users;
    }

    public List<User> getDeletedUsers() {
        String sql = "SELECT UserId, FirstName, LastName, Email, Password, UserRole FROM Users WHERE IsDeleted = 1";

        List<User> users = new ArrayList<>();

        try (Connection con = DBConnector.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return users;
    }

    public void deleteUser(String userId) {
        String sql = "UPDATE Users SET IsDeleted = 1 WHERE UserId = ?";

        try (Connection con = DBConnector.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.executeUpdate();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
    private User mapUser(ResultSet rs) throws SQLException {
        String id = rs.getString("UserId");
        String first = rs.getString("Firstname");
        String last = rs.getString("Lastname");
        String email = rs.getString("Email");
        String password = rs.getString("Password");
        String role = rs.getString("UserRole");

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(id, first, last, email, password);
        } else if ("COORDINATOR".equalsIgnoreCase(role)) {
            return new EventCoordinator(id, first, last, email, password);
        } else {
            String name = first;
            if (last != null && !last.isBlank()) {
                name = first + " " + last;
            }
            return new RegularUser(id, name, email);
        }
    }
}

