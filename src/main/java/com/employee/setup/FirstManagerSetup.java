package com.employee.setup;

import com.employee.config.DatabaseConnection;
import com.employee.exception.AppException;
import com.employee.exception.DatabaseException;
import com.employee.util.PasswordUtil;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * One-time tool that creates the FIRST manager account.
 *
 * Run it once from IntelliJ. The password is typed into a masked field,
 * hashed with BCrypt, and only the hash is saved. It refuses to run if a
 * manager already exists, so it cannot be used later to add extra managers.
 *
 * The SQL is inside this class on purpose: it is an admin tool, not part of
 * the normal DAO layer.
 */
public class FirstManagerSetup {

    private static final Logger LOGGER = Logger.getLogger(FirstManagerSetup.class.getName());
    private static final String TITLE = "First Manager Setup";

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.]{3,50}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_PASSWORD = 8;
    private static final int MAX_PASSWORD = 64; // BCrypt only uses the first 72 bytes

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FirstManagerSetup::run);
    }

    private static void run() {
        try {
            if (managerExists()) {
                info("A manager account already exists.\n"
                        + "This tool only creates the FIRST manager, so nothing was changed.");
                return;
            }

            JTextField usernameField = new JTextField(20);
            JTextField nameField = new JTextField(20);
            JTextField emailField = new JTextField(20);
            JTextField departmentField = new JTextField("Management", 20);
            JPasswordField passwordField = new JPasswordField(20);
            JPasswordField confirmField = new JPasswordField(20);

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Username:"));
            form.add(usernameField);
            form.add(new JLabel("Full name:"));
            form.add(nameField);
            form.add(new JLabel("Email:"));
            form.add(emailField);
            form.add(new JLabel("Department:"));
            form.add(departmentField);
            form.add(new JLabel("Password (min 8):"));
            form.add(passwordField);
            form.add(new JLabel("Confirm password:"));
            form.add(confirmField);

            String username;
            String fullName;
            String email;
            String department;
            String password;

            // Keep showing the form until the input is valid or the user cancels.
            while (true) {
                int choice = JOptionPane.showConfirmDialog(null, form, TITLE,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (choice != JOptionPane.OK_OPTION) {
                    return;
                }

                username = usernameField.getText().trim();
                fullName = nameField.getText().trim();
                email = emailField.getText().trim();
                department = departmentField.getText().trim();
                char[] pw = passwordField.getPassword();
                char[] confirm = confirmField.getPassword();

                List<String> errors = validate(username, fullName, email, department, pw, confirm);
                password = errors.isEmpty() ? new String(pw) : null;
                Arrays.fill(pw, '\0');
                Arrays.fill(confirm, '\0');

                if (errors.isEmpty()) {
                    break;
                }
                error(String.join("\n", errors));
            }

            createManager(username, password, fullName, email, department);
            info("Manager account created successfully.\n\nYou can now log in as: " + username);

        } catch (AppException e) {
            error(e.getMessage());
        }
    }

    private static List<String> validate(String username, String fullName, String email,
                                         String department, char[] password, char[] confirm) {
        List<String> errors = new ArrayList<>();
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            errors.add("- Username must be 3-50 characters: letters, digits, underscore or dot.");
        }
        if (fullName.isEmpty() || fullName.length() > 100) {
            errors.add("- Full name is required (max 100 characters).");
        }
        if (email.length() > 120 || !EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("- Enter a valid email address.");
        }
        if (department.isEmpty() || department.length() > 80) {
            errors.add("- Department is required (max 80 characters).");
        }
        if (password.length < MIN_PASSWORD || password.length > MAX_PASSWORD) {
            errors.add("- Password must be " + MIN_PASSWORD + "-" + MAX_PASSWORD + " characters long.");
        } else if (!hasLetterAndDigit(password)) {
            errors.add("- Password must contain at least one letter and one digit.");
        }
        if (!Arrays.equals(password, confirm)) {
            errors.add("- Password and confirmation do not match.");
        }
        return errors;
    }

    private static boolean hasLetterAndDigit(char[] password) {
        boolean letter = false;
        boolean digit = false;
        for (char c : password) {
            if (Character.isLetter(c)) {
                letter = true;
            }
            if (Character.isDigit(c)) {
                digit = true;
            }
        }
        return letter && digit;
    }

    private static boolean managerExists() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'MANAGER'";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not check for an existing manager", e);
            throw new DatabaseException("Could not read the users table. Did you run schema.sql?", e);
        }
    }

    /** Inserts into users and employees inside ONE transaction: both succeed or neither does. */
    private static void createManager(String username, String password, String fullName,
                                      String email, String department) {
        String insertUser = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, 'MANAGER')";
        String insertProfile = "INSERT INTO employees (user_id, employee_name, email, department) "
                + "VALUES (?, ?, ?, ?)";
        String hash = PasswordUtil.hash(password);

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                long userId;
                try (PreparedStatement ps = con.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, hash);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No generated key returned for the new user.");
                        }
                        userId = keys.getLong(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(insertProfile)) {
                    ps.setLong(1, userId);
                    ps.setString(2, fullName);
                    ps.setString(3, email);
                    ps.setString(4, department);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                DatabaseConnection.rollbackQuietly(con);
                throw e;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AppException("That username or email address is already in use.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not create the first manager", e);
            throw new DatabaseException("Could not create the manager account. See the console log.", e);
        }
    }

    private static void info(String message) {
        JOptionPane.showMessageDialog(null, message, TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    private static void error(String message) {
        JOptionPane.showMessageDialog(null, message, TITLE, JOptionPane.ERROR_MESSAGE);
    }
}