package com.employee.ui;

import com.employee.exception.AppException;
import com.employee.model.DashboardStats;
import com.employee.service.DashboardService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dashboard with summary cards. The same class is used for both roles:
 * a manager sees 6 cards (company-wide), an employee sees 5 (own tasks only).
 * Numbers are loaded in the background with a SwingWorker so the window never freezes.
 */
public class DashboardPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(DashboardPanel.class.getName());

    private final boolean managerView;
    private final DashboardService service = new DashboardService();

    private final JLabel employeesValue = valueLabel();
    private final JLabel totalValue = valueLabel();
    private final JLabel pendingValue = valueLabel();
    private final JLabel inProgressValue = valueLabel();
    private final JLabel completedValue = valueLabel();
    private final JLabel overdueValue = valueLabel();

    private final Theme.RoundedButton refreshButton = Theme.secondaryButton("Refresh");
    private final JLabel statusLabel = Theme.label(" ", Theme.FONT_SMALL, Theme.MUTED);

    public DashboardPanel(boolean managerView, String fullName) {
        this.managerView = managerView;
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        add(buildHeader(fullName), BorderLayout.NORTH);
        add(buildCards(), BorderLayout.CENTER);

        refreshButton.addActionListener(e -> refresh());
    }

    private static JLabel valueLabel() {
        return Theme.label("-", Theme.FONT_BIG, Theme.TEXT);
    }

    private JPanel buildHeader(String fullName) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(Theme.label((managerView ? "Welcome back, " : "Welcome, ") + fullName,
                Theme.FONT_TITLE, Theme.TEXT));
        titles.add(Box.createVerticalStrut(4));
        titles.add(Theme.label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
                Theme.FONT_BODY, Theme.MUTED));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.add(statusLabel);
        actions.add(refreshButton);

        header.add(titles, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCards() {
        JPanel grid = new JPanel(new GridLayout(0, 3, 16, 16));
        grid.setOpaque(false);
        if (managerView) {
            grid.add(statCard("Total employees", employeesValue, Theme.PRIMARY));
        }
        grid.add(statCard(managerView ? "Total tasks" : "Assigned to me", totalValue, Theme.NAVY));
        grid.add(statCard("Pending", pendingValue, Theme.WARNING));
        grid.add(statCard("In progress", inProgressValue, Theme.INFO));
        grid.add(statCard("Completed", completedValue, Theme.SUCCESS));
        grid.add(statCard("Overdue", overdueValue, Theme.DANGER));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(grid, BorderLayout.NORTH);   // NORTH keeps the cards from stretching tall
        return wrapper;
    }

    private JPanel statCard(String caption, JLabel value, Color accent) {
        Theme.CardPanel card = new Theme.CardPanel(new BorderLayout(0, 6));
        value.setForeground(accent);
        card.add(Theme.label(caption, Theme.FONT_BODY, Theme.MUTED), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(200, 120));
        return card;
    }

    /** Reloads the numbers from MySQL without freezing the window. */
    public void refresh() {
        refreshButton.setEnabled(false);
        statusLabel.setText("Loading...");

        new SwingWorker<DashboardStats, Void>() {
            @Override
            protected DashboardStats doInBackground() {
                // Runs on a background thread: the database call happens here.
                return managerView ? service.getManagerStats() : service.getMyStats();
            }

            @Override
            protected void done() {
                // Runs on the Swing thread: safe to update the screen here.
                try {
                    show(get());
                    statusLabel.setText("Updated " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    statusLabel.setText(" ");
                    Throwable cause = e.getCause();
                    if (cause instanceof AppException) {
                        Theme.showError(DashboardPanel.this, cause.getMessage());
                    } else {
                        LOGGER.log(Level.SEVERE, "Unexpected dashboard error", cause);
                        Theme.showError(DashboardPanel.this, "Something went wrong while loading the dashboard.");
                    }
                } finally {
                    refreshButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void show(DashboardStats stats) {
        employeesValue.setText(String.valueOf(stats.totalEmployees()));
        totalValue.setText(String.valueOf(stats.totalTasks()));
        pendingValue.setText(String.valueOf(stats.pendingTasks()));
        inProgressValue.setText(String.valueOf(stats.inProgressTasks()));
        completedValue.setText(String.valueOf(stats.completedTasks()));
        overdueValue.setText(String.valueOf(stats.overdueTasks()));
    }
}