package com.tanzeel.polling.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WelcomeFrame extends JFrame {
    public WelcomeFrame() {
        Theme.prepareFrame(this, "Polling System", 820, 560);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.NAVY);
        header.setBorder(new EmptyBorder(24, 32, 24, 32));
        JLabel brand = new JLabel("POLLING SYSTEM");
        brand.setForeground(Color.WHITE);
        brand.setFont(Theme.font(Font.BOLD, 22));
        JLabel tag = new JLabel("Secure desktop polling with persistent results");
        tag.setForeground(new Color(220, 231, 241));
        tag.setFont(Theme.font(Font.PLAIN, 13));
        JPanel brandStack = new JPanel();
        brandStack.setOpaque(false);
        brandStack.setLayout(new BoxLayout(brandStack, BoxLayout.Y_AXIS));
        brandStack.add(brand);
        brandStack.add(Box.createVerticalStrut(5));
        brandStack.add(tag);
        header.add(brandStack, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Theme.BG);
        body.setBorder(new EmptyBorder(34, 34, 34, 34));

        JPanel card = Theme.card(new BorderLayout(0, 22));
        card.setPreferredSize(new Dimension(680, 360));

        JPanel intro = new JPanel();
        intro.setOpaque(false);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        JLabel title = Theme.heading("Choose how you want to continue", 26);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = Theme.muted("Create a voter account, sign in to vote, or open the administrator dashboard.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        intro.add(title);
        intro.add(Box.createVerticalStrut(8));
        intro.add(subtitle);
        card.add(intro, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(1, 3, 14, 14));
        actions.setOpaque(false);
        actions.add(actionCard("Voter Login", "Sign in and vote in currently open polls.", "Sign In", () -> openAuth(AuthFrame.Mode.USER_LOGIN)));
        actions.add(actionCard("Create Account", "Register a voter account with a protected password.", "Register", () -> openAuth(AuthFrame.Mode.REGISTER)));
        actions.add(actionCard("Administrator", "Create polls, open or close voting and review results.", "Admin Login", () -> openAuth(AuthFrame.Mode.ADMIN_LOGIN)));
        card.add(actions, BorderLayout.CENTER);

        JLabel privacy = Theme.muted("Data is stored locally in SQLite on this computer. Passwords are stored as BCrypt hashes.");
        card.add(privacy, BorderLayout.SOUTH);
        body.add(card);
        add(body, BorderLayout.CENTER);
    }

    private JPanel actionCard(String title, String description, String buttonText, Runnable action) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(Theme.BLUE_SOFT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel heading = Theme.heading(title, 17);
        JTextArea copy = new JTextArea(description);
        copy.setEditable(false);
        copy.setOpaque(false);
        copy.setWrapStyleWord(true);
        copy.setLineWrap(true);
        copy.setForeground(Theme.MUTED);
        copy.setFont(Theme.font(Font.PLAIN, 13));
        text.add(heading);
        text.add(Box.createVerticalStrut(8));
        text.add(copy);
        panel.add(text, BorderLayout.CENTER);

        JButton button = title.equals("Create Account") ? Theme.accentButton(buttonText) : Theme.primaryButton(buttonText);
        button.addActionListener(event -> action.run());
        panel.add(button, BorderLayout.SOUTH);
        return panel;
    }

    private void openAuth(AuthFrame.Mode mode) {
        new AuthFrame(mode).setVisible(true);
        dispose();
    }
}
