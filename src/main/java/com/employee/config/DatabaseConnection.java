package com.employee.config;

import com.employee.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ONE place in the project that opens database connections.
 * DAO classes call DatabaseConnection.getConnection() and close the
 * connection themselves with try-with-resources.
 */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static DatabaseConfig config;

    private DatabaseConnection() {
        // utility class: no objects needed
    }

    private static synchronized DatabaseConfig config() {
        if (config == null) {
            config = DatabaseConfig.load();
        }
        return config;
    }

    /**
     * Opens a NEW connection. The caller must close it (try-with-resources).
     *
     * @throws DatabaseException with a user-friendly message if it fails
     */
    public static Connection getConnection() {
        DatabaseConfig cfg = config();
        try {
            return DriverManager.getConnection(cfg.getUrl(), cfg.getUsername(), cfg.getPassword());
        } catch (SQLException e) {
            // Technical details go to the log (no password is included).
            LOGGER.log(Level.SEVERE, "Database connection failed. SQLState=" + e.getSQLState()
                    + ", errorCode=" + e.getErrorCode() + ", message=" + e.getMessage());
            throw new DatabaseException(friendlyMessage(e), e);
        }
    }

    /** Turns a technical SQL error into a sentence a normal user can understand. */
    private static String friendlyMessage(SQLException e) {
        if (e.getErrorCode() == 1045) {
            return "Database login failed. Check db.username and db.password in application.properties.";
        }
        if (e.getErrorCode() == 1049) {
            return "The database 'employee_task_db' was not found. Run database/schema.sql first.";
        }
        if ("08S01".equals(e.getSQLState()) || "08001".equals(e.getSQLState())) {
            return "Cannot reach the MySQL server. Make sure the MySQL80 service is running "
                    + "and the port in db.url is correct.";
        }
        return "Could not connect to the database. Please check the settings and try again.";
    }

    /** Used inside catch blocks to undo a transaction without hiding the real error. */
    public static void rollbackQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Rollback failed", ex);
        }
    }
}