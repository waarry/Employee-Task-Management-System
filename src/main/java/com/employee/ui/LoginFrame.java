package com.employee.ui;

import com.employee.exception.AppException;
import com.employee.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The login screen. It never talks to the database itself:
 * it calls AuthService.login() inside a SwingWorker.
 * (BCrypt is slow on purpose, so doing it on the Swing thread would freeze the window.)
 */
public class LoginFrame extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());

    private final AuthService authService = new AuthService();

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JCheckBox showPassword = new JCheckBox("Show password");
    private final JLabel errorLabel = Theme.label(" ", Theme.FONT_SMALL, Theme.DANGER);
    private final Theme.RoundedButton loginButton = Theme.primaryButton("Sign in");
    private final char defaultEchoChar = passwordField.getEchoChar();

    public LoginFrame() {
        setTitle("Employee Task Management System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(780, 480));
        setSize(900, 540);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));

        add(buildBrandPanel());
        add(buildFormPanel());

        getRootPane().setDefaultButton(loginButton);   // pressing Enter clicks "Sign in"
        loginButton.addActionListener(e -> attemptLogin());
        showPassword.addActionListener(e ->
                passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : defaultEchoChar));
    }

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.NAVY);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.add(Theme.label("Employee Task", Theme.FONT_TITLE, Color.WHITE));
        inner.add(Theme.label("Management System", Theme.FONT_TITLE, Color.WHITE));
        inner.add(Box.createVerticalStrut(16));
        inner.add(Theme.label("Assign tasks. Track progress.", Theme.FONT_BODY, Theme.SIDEBAR_TEXT));
        inner.add(Theme.label("Never miss a deadline.", Theme.FONT_BODY, Theme.SIDEBAR_TEXT));

        panel.add(inner);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        Theme.styleField(usernameField);
        Theme.styleField(passwordField);

        JLabel usernameLabel = Theme.label("Username", Theme.FONT_BOLD, Theme.TEXT);
        usernameLabel.setLabelFor(usernameField);   // helps screen readers
        JLabel passwordLabel = Theme.label("Password", Theme.FONT_BOLD, Theme.TEXT);
        passwordLabel.setLabelFor(passwordField);

        showPassword.setOpaque(false);
        showPassword.setFont(Theme.FONT_SMALL);
        showPassword.setForeground(Theme.MUTED);
        showPassword.setAlignmentX(LEFT_ALIGNMENT);
        showPassword.setFocusPainted(false);

        Theme.fullWidth(loginButton);
        loginButton.setPreferredSize(new Dimension(340, 44));

        form.add(Theme.label("Sign in", Theme.FONT_TITLE, Theme.TEXT));
        form.add(Box.createVerticalStrut(4));
        form.add(Theme.label("Enter your account details to continue.", Theme.FONT_BODY, Theme.MUTED));
        form.add(Box.createVerticalStrut(24));
        form.add(usernameLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(14));
        form.add(passwordLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(6));
        form.add(showPassword);
        form.add(Box.createVerticalStrut(10));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(10));
        form.add(loginButton);

        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(form);
        return panel;
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        char[] typed = passwordField.getPassword();
        String password = new String(typed);
        Arrays.fill(typed, '\0');   // wipe the char[] copy

        errorLabel.setText(" ");
        setBusy(true);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                authService.login(username, password);   // slow part: database + BCrypt
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    setBusy(false);
                    return;
                } catch (ExecutionException e) {
                    setBusy(false);
                    Throwable cause = e.getCause();
                    if (cause instanceof AppException) {
                        showLoginError(cause.getMessage());
                    } else {
                        LOGGER.log(Level.SEVERE, "Unexpected login error", cause);
                        showLoginError("Something went wrong. Please try again.");
                    }
                    passwordField.setText("");
                    passwordField.requestFocusInWindow();
                    return;
                }
                // Success: the session is now set, so open the main window.
                passwordField.setText("");
                dispose();
                new MainFrame().setVisible(true);
            }
        }.execute();
    }

    private void showLoginError(String message) {
        errorLabel.setText("<html><body style='width:300px'>" + message + "</body></html>");
    }

    /** Disables the form while the login is running. */
    private void setBusy(boolean busy) {
        usernameField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
        showPassword.setEnabled(!busy);
        loginButton.setEnabled(!busy);
        loginButton.setText(busy ? "Signing in..." : "Sign in");
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }
}