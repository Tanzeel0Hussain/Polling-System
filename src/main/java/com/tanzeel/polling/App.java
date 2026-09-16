package com.tanzeel.polling;

import com.tanzeel.polling.data.Database;
import com.tanzeel.polling.service.AuthService;
import com.tanzeel.polling.ui.Theme;
import com.tanzeel.polling.ui.WelcomeFrame;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public final class App {
    private App() {}

    public static void main(String[] args) {
        Database.initialize();
        SwingUtilities.invokeLater(() -> {
            Theme.apply();
            AuthService authService = new AuthService();
            if (!authService.hasAdmin() && !createFirstAdmin(authService)) {
                System.exit(0);
            }
            new WelcomeFrame().setVisible(true);
        });
    }

    private static boolean createFirstAdmin(AuthService authService) {
        while (true) {
            JTextField usernameField = new JTextField("admin", 18);
            JPasswordField passwordField = new JPasswordField(18);
            JPasswordField confirmField = new JPasswordField(18);

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(Color.WHITE);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            JLabel intro = new JLabel("Create the first administrator account");
            intro.setFont(Theme.font(Font.BOLD, 16));
            panel.add(intro, gbc);

            gbc.gridwidth = 1;
            gbc.gridy++;
            panel.add(new JLabel("Username"), gbc);
            gbc.gridx = 1;
            panel.add(usernameField, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            panel.add(new JLabel("Password"), gbc);
            gbc.gridx = 1;
            panel.add(passwordField, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            panel.add(new JLabel("Confirm password"), gbc);
            gbc.gridx = 1;
            panel.add(confirmField, gbc);

            int option = JOptionPane.showConfirmDialog(null, panel, "Polling System — First Run",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option != JOptionPane.OK_OPTION) return false;

            char[] password = passwordField.getPassword();
            char[] confirm = confirmField.getPassword();
            try {
                if (!Arrays.equals(password, confirm)) {
                    throw new IllegalArgumentException("Passwords do not match.");
                }
                authService.createFirstAdmin(usernameField.getText(), password);
                JOptionPane.showMessageDialog(null,
                        "Administrator account created. You can now sign in.",
                        "Setup complete", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (RuntimeException exception) {
                JOptionPane.showMessageDialog(null, exception.getMessage(), "Setup error", JOptionPane.ERROR_MESSAGE);
            } finally {
                Arrays.fill(password, '\0');
                Arrays.fill(confirm, '\0');
            }
        }
    }
}
