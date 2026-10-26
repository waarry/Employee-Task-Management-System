package com.employee.dao;

import com.employee.config.DatabaseConnection;
import com.employee.exception.DatabaseException;
import com.employee.model.Role;
import com.employee.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO = Data Access Object. This class holds ALL SQL about the users table.
 * Other layers never write SQL; they call methods of this class.
 */
public class UserDao {

    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());

    /**
     * Finds one user by username (MySQL compares usernames case-insensitively).
     * Returns Optional.empty() when no such user exists.
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT u.user_id, u.username, u.password_hash, u.role, u.is_active, "
                + "e.employee_id, e.employee_name "
                + "FROM users u LEFT JOIN employees e ON e.user_id = u.user_id "
                + "WHERE u.username = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByUsername failed", e);
            throw new DatabaseException("Could not read user data. Please try again.", e);
        }
    }

    /** Converts the current row of the ResultSet into a User object. */
    private User mapRow(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        String fullName = rs.getString("employee_name");
        return new User(
                rs.getInt("user_id"),
                username,
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                rs.getBoolean("is_active"),
                rs.getInt("employee_id"),            // 0 when there is no profile row
                fullName != null ? fullName : username);
    }
}