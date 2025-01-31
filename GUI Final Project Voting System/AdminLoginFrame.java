//package myproject;

import javax.swing.*;
import java.awt.*;

public class AdminLoginFrame extends JFrame {
    public AdminLoginFrame() {
        setTitle("Admin Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (PollingSystem.adminUsername.equals(username) && PollingSystem.adminPassword.equals(password)) {
                new AdminPanelFrame().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        });

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);
    }
}
