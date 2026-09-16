package com.tanzeel.polling.ui;

import com.tanzeel.polling.model.User;
import com.tanzeel.polling.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

public class AuthFrame extends JFrame {
    public enum Mode { USER_LOGIN, ADMIN_LOGIN, REGISTER }

    private final Mode mode;
    private final AuthService authService = new AuthService();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JLabel messageLabel = new JLabel(" ");

    public AuthFrame(Mode mode) {
        this.mode = mode;
        Theme.prepareFrame(this, titleForMode(), 560, mode == Mode.REGISTER ? 520 : 460);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.NAVY);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));
        JLabel heading = new JLabel(titleForMode());
        heading.setForeground(Color.WHITE);
        heading.setFont(Theme.font(Font.BOLD, 22));
        header.add(heading, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Theme.BG);
        body.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel card = Theme.card(new GridBagLayout());
        card.setPreferredSize(new Dimension(440, mode == Mode.REGISTER ? 350 : 290));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel intro = Theme.muted(mode == Mode.REGISTER
                ? "Create a voter account. Usernames are unique and passwords are hashed before storage."
                : "Enter your credentials to continue.");
        card.add(intro, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        addField(card, gbc, "Username", usernameField);
        gbc.gridy++;
        addField(card, gbc, "Password", passwordField);

        if (mode == Mode.REGISTER) {
            gbc.gridy++;
            addField(card, gbc, "Confirm Password", confirmField);
        }

        gbc.gridy++;
        gbc.gridwidth = 2;
        messageLabel.setForeground(Theme.DANGER);
        messageLabel.setFont(Theme.font(Font.BOLD, 12));
        card.add(messageLabel, gbc);

        gbc.gridy++;
        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        JButton backButton = Theme.secondaryButton("Back");
        backButton.addActionListener(event -> back());
        JButton submitButton = mode == Mode.REGISTER ? Theme.accentButton("Create Account") : Theme.primaryButton("Sign In");
        submitButton.addActionListener(event -> submit());
        buttons.add(backButton);
        buttons.add(submitButton);
        card.add(buttons, gbc);

        body.add(card);
        add(body, BorderLayout.CENTER);
        getRootPane().setDefaultButton(submitButton);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.font(Font.BOLD, 13));
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setPreferredSize(new Dimension(220, 36));
        panel.add(field, gbc);
        gbc.gridx = 0;
    }

    private void submit() {
        messageLabel.setText(" ");
        char[] password = passwordField.getPassword();
        char[] confirm = confirmField.getPassword();
        try {
            if (mode == Mode.REGISTER) {
                if (!Arrays.equals(password, confirm)) throw new IllegalArgumentException("Passwords do not match.");
                authService.registerUser(usernameField.getText(), password);
                JOptionPane.showMessageDialog(this, "Account created successfully. You can now sign in.",
                        "Registration complete", JOptionPane.INFORMATION_MESSAGE);
                new AuthFrame(Mode.USER_LOGIN).setVisible(true);
                dispose();
                return;
            }

            User.Role expectedRole = mode == Mode.ADMIN_LOGIN ? User.Role.ADMIN : User.Role.USER;
            Optional<User> user = authService.authenticate(usernameField.getText(), password, expectedRole);
            if (user.isEmpty()) {
                messageLabel.setText("Invalid username/password for this account type.");
                return;
            }

            if (expectedRole == User.Role.ADMIN) {
                new AdminDashboardFrame(user.get()).setVisible(true);
            } else {
                new UserDashboardFrame(user.get()).setVisible(true);
            }
            dispose();
        } catch (RuntimeException exception) {
            messageLabel.setText(exception.getMessage());
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
        }
    }

    private void back() {
        new WelcomeFrame().setVisible(true);
        dispose();
    }

    private String titleForMode() {
        return switch (mode) {
            case USER_LOGIN -> "Voter Sign In";
            case ADMIN_LOGIN -> "Administrator Sign In";
            case REGISTER -> "Create Voter Account";
        };
    }
}
