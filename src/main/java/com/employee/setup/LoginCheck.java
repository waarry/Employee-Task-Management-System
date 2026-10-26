package com.employee.setup;

import com.employee.exception.AppException;
import com.employee.model.User;
import com.employee.service.AuthService;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.util.Arrays;

/** Manual test for Phase 3: type a username and password and see the result. */
public class LoginCheck {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);
            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Username:"));
            form.add(usernameField);
            form.add(new JLabel("Password:"));
            form.add(passwordField);

            int choice = JOptionPane.showConfirmDialog(null, form, "Login Check",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            char[] pw = passwordField.getPassword();
            try {
                User user = new AuthService().login(usernameField.getText(), new String(pw));
                JOptionPane.showMessageDialog(null,
                        "Login OK\nName: " + user.getFullName() + "\nRole: " + user.getRole(),
                        "Login Check", JOptionPane.INFORMATION_MESSAGE);
            } catch (AppException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(),
                        "Login failed", JOptionPane.ERROR_MESSAGE);
            } finally {
                Arrays.fill(pw, '\0');
            }
        });
    }
}