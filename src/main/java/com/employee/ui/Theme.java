package com.employee.ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * One place for colors, fonts and ready-made styled components.
 * Every screen uses Theme, so the whole application looks consistent.
 */
public final class Theme {

    private static final Logger LOGGER = Logger.getLogger(Theme.class.getName());

    // ----- Colors (darker shades are used for text so it stays readable) -----
    public static final Color NAVY = new Color(0x0F1F3D);
    public static final Color NAVY_LIGHT = new Color(0x1E3A68);
    public static final Color BACKGROUND = new Color(0xF3F5F9);
    public static final Color CARD = Color.WHITE;
    public static final Color PRIMARY = new Color(0x2563EB);
    public static final Color PRIMARY_DARK = new Color(0x1D4ED8);
    public static final Color TEXT = new Color(0x111827);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xD1D5DB);
    public static final Color SUCCESS = new Color(0x15803D);
    public static final Color WARNING = new Color(0xB45309);
    public static final Color DANGER = new Color(0xB91C1C);
    public static final Color DANGER_DARK = new Color(0x991B1B);
    public static final Color INFO = new Color(0x0369A1);
    public static final Color SIDEBAR_TEXT = new Color(0xCBD5E1);
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // ----- Fonts -----
    private static final String FAMILY = "Segoe UI";
    public static final Font FONT_BODY = new Font(FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font(FAMILY, Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font FONT_HEADING = new Font(FAMILY, Font.BOLD, 18);
    public static final Font FONT_TITLE = new Font(FAMILY, Font.BOLD, 24);
    public static final Font FONT_BIG = new Font(FAMILY, Font.BOLD, 36);

    private Theme() {
    }

    /** Uses the same look on every computer (Java's built-in "Metal" look). */
    public static void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not set the look and feel", e);
        }
    }

    // ----- Factory methods -----

    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public static RoundedButton primaryButton(String text) {
        return new RoundedButton(text, PRIMARY, PRIMARY_DARK, Color.WHITE);
    }

    public static RoundedButton secondaryButton(String text) {
        return new RoundedButton(text, new Color(0xE5E7EB), new Color(0xD1D5DB), TEXT);
    }

    public static RoundedButton dangerButton(String text) {
        return new RoundedButton(text, DANGER, DANGER_DARK, Color.WHITE);
    }

    /** A flat, left-aligned button for the navigation sidebar. */
    public static RoundedButton sidebarButton(String text) {
        RoundedButton button = new RoundedButton(text, TRANSPARENT, NAVY_LIGHT, Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        fullWidth(button);
        return button;
    }

    /** Makes a component stretch across the full width inside a vertical BoxLayout. */
    public static void fullWidth(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
    }

    public static void styleField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(340, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    // ----- Message dialogs -----

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Returns true if the user clicked Yes. */
    public static boolean confirm(Component parent, String message) {
        int choice = JOptionPane.showConfirmDialog(parent, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    // ----- Custom components -----

    /** A button with rounded corners, a hover color and a visible keyboard-focus outline. */
    public static class RoundedButton extends JButton {

        private Color normal;
        private Color hover;
        private boolean hovering;

        public RoundedButton(String text, Color normal, Color hover, Color textColor) {
            super(text);
            this.normal = normal;
            this.hover = hover;
            setFont(FONT_BOLD);
            setForeground(textColor);
            setContentAreaFilled(false);   // we paint the background ourselves
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovering = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovering = false;
                    repaint();
                }
            });
        }

        public void setColors(Color normal, Color hover) {
            this.normal = normal;
            this.hover = hover;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!isEnabled()) {
                g2.setColor(new Color(0xE5E7EB));
            } else {
                g2.setColor(hovering ? hover : normal);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            if (isFocusOwner()) {
                g2.setColor(new Color(0x93C5FD));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);   // draws the text
        }
    }

    /** A white panel with rounded corners and a thin border, used for dashboard cards. */
    public static class CardPanel extends JPanel {

        public CardPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}