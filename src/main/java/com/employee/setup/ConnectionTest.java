package com.employee.setup;

import com.employee.config.DatabaseConnection;
import com.employee.exception.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Quick check that the application can reach MySQL with the settings
 * in application.properties. Prints the result in the Run console.
 */
public class ConnectionTest {

    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        try (Connection con = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT VERSION(), DATABASE(), CURRENT_USER()");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("MySQL version : " + rs.getString(1));
                System.out.println("Database      : " + rs.getString(2));
                System.out.println("Logged in as  : " + rs.getString(3));
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                System.out.println("Tables found  : " + rs.getInt(1) + " (expected 4)");
            }

            System.out.println("RESULT: CONNECTION OK");

        } catch (AppException e) {
            System.out.println("RESULT: FAILED -> " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("RESULT: FAILED -> " + e.getMessage());
        }
    }
}