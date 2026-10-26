package com.employee;

import com.employee.ui.LoginFrame;
import com.employee.ui.Theme;

import javax.swing.SwingUtilities;

/** Entry point of the application: it simply opens the login screen. */
public class Main {

    public static void main(String[] args) {
        // All Swing screens must be created on the Swing thread (the "Event Dispatch Thread").
        SwingUtilities.invokeLater(() -> {
            Theme.installLookAndFeel();
            new LoginFrame().setVisible(true);
        });
    }
}