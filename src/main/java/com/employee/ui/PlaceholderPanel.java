package com.employee.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * TEMPORARY screen for sections that are built in later phases.
 * Each one is replaced by the real screen, and this file is deleted in Phase 8.
 */
public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String title, String builtInPhase) {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        Theme.CardPanel card = new Theme.CardPanel(new BorderLayout());
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Theme.label(title, Theme.FONT_TITLE, Theme.TEXT));
        text.add(javax.swing.Box.createVerticalStrut(8));
        text.add(Theme.label("This screen will be built in " + builtInPhase + ".",
                Theme.FONT_BODY, Theme.MUTED));
        card.add(text, BorderLayout.NORTH);

        add(card, BorderLayout.NORTH);
    }
}