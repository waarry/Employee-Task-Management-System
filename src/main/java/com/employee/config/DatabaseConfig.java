package com.employee.config;

import com.employee.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds the database settings (URL, username, password).
 *
 * Order of priority:
 *   1. Environment variables ETM_DB_URL, ETM_DB_USER, ETM_DB_PASSWORD
 *   2. The file application.properties (keys db.url, db.username, db.password)
 *
 * The password is never printed or logged.
 */
public final class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static final String PROPERTIES_FILE = "application.properties";

    private final String url;
    private final String username;
    private final String password;

    private DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /** Reads the settings and checks that none are missing. */
    public static DatabaseConfig load() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not read " + PROPERTIES_FILE, e);
        }

        String url = firstNonBlank(System.getenv("ETM_DB_URL"), props.getProperty("db.url"));
        String user = firstNonBlank(System.getenv("ETM_DB_USER"), props.getProperty("db.username"));
        String pass = firstNonBlank(System.getenv("ETM_DB_PASSWORD"), props.getProperty("db.password"));

        if (url == null || user == null || pass == null) {
            throw new DatabaseException(
                    "Database settings are missing. Create src/main/resources/application.properties "
                            + "(copy application.properties.example) and fill in your values.");
        }
        if ("CHANGE_ME".equals(pass)) {
            throw new DatabaseException(
                    "Please replace CHANGE_ME in application.properties with your etm_user password.");
        }
        return new DatabaseConfig(url.trim(), user.trim(), pass);
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /** Deliberately leaves out the password. */
    @Override
    public String toString() {
        return "DatabaseConfig{url=" + url + ", username=" + username + "}";
    }
}