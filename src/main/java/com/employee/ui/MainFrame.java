package com.employee.ui;

import com.employee.model.Role;
import com.employee.model.User;
import com.employee.service.AuthService;
import com.employee.service.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The main window after login: a dark navy sidebar on the left and a
 * content area on the right. A CardLayout shows one screen at a time.
 * The sidebar items depend on the role of the logged-in user.
 */
public class MainFrame extends JFrame {

    private static final String DASHBOARD = "dashboard";

    private record NavItem(String key, String label, JPanel panel) {
    }

    private final User user;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, Theme.RoundedButton> navButtons = new LinkedHashMap<>();
    private DashboardPanel dashboardPanel;

    public MainFrame() {
        // Throws AuthenticationException if nobody is logged in: no access without login.
        this.user = SessionContext.requireLogin();

        setTitle("Employee Task Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 620));
        setSize(1180, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        List<NavItem> items = createNavItems();
        for (NavItem item : items) {
            content.add(item.panel(), item.key());
        }
        add(buildSidebar(items), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        showPage(DASHBOARD);
    }

    private List<NavItem> createNavItems() {
        boolean manager = user.getRole() == Role.MANAGER;
        dashboardPanel = new DashboardPanel(manager, user.getFullName());

        List<NavItem> items = new ArrayList<>();
        items.add(new NavItem(DASHBOARD, "Dashboard", dashboardPanel));
        if (manager) {
            items.add(new NavItem("employees", "Employees",
                    new PlaceholderPanel("Employee Management", "Phase 5")));
            items.add(new NavItem("tasks", "Tasks",
                    new PlaceholderPanel("Task Management", "Phase 6")));
            items.add(new NavItem("reports", "Reports",
                    new PlaceholderPanel("Reports", "Phase 8")));
        } else {
            items.add(new NavItem("mytasks", "My Tasks",
                    new PlaceholderPanel("My Tasks", "Phase 7")));
        }
        items.add(new NavItem("profile", "My Profile",
                new PlaceholderPanel("My Profile", "Phase 5")));
        return items;
    }

    private JPanel buildSidebar(List<NavItem> items) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.NAVY);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 20, 16));
        sidebar.setPreferredSize(new Dimension(240, 0));

        String roleText = user.getRole() == Role.MANAGER ? "Manager" : "Employee";
        sidebar.add(Theme.label("Task Manager", Theme.FONT_HEADING, java.awt.Color.WHITE));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(Theme.label(user.getFullName(), Theme.FONT_BODY, Theme.SIDEBAR_TEXT));
        sidebar.add(Theme.label(roleText, Theme.FONT_SMALL, Theme.SIDEBAR_TEXT));
        sidebar.add(Box.createVerticalStrut(28));

        for (NavItem item : items) {
            Theme.RoundedButton button = Theme.sidebarButton(item.label());
            button.addActionListener(e -> showPage(item.key()));
            navButtons.put(item.key(), button);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        Theme.RoundedButton logoutButton = Theme.dangerButton("Log out");
        Theme.fullWidth(logoutButton);
        logoutButton.addActionListener(e -> logout());
        sidebar.add(logoutButton);
        return sidebar;
    }

    /** Shows one screen and highlights its sidebar button. */
    private void showPage(String key) {
        cardLayout.show(content, key);
        navButtons.forEach((buttonKey, button) -> {
            boolean selected = buttonKey.equals(key);
            button.setColors(selected ? Theme.PRIMARY : Theme.TRANSPARENT,
                    selected ? Theme.PRIMARY_DARK : Theme.NAVY_LIGHT);
        });
        if (DASHBOARD.equals(key)) {
            dashboardPanel.refresh();
        }
    }

    private void logout() {
        if (!Theme.confirm(this, "Do you want to log out?")) {
            return;
        }
        new AuthService().logout();   // clears the session
        dispose();
        new LoginFrame().setVisible(true);
    }
}