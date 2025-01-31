//package myproject;

import javax.swing.*;
import java.awt.*;

public class UserLoginFrame extends JFrame {
    public UserLoginFrame() {
        setTitle("User Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (PollingSystem.users.containsKey(username) && PollingSystem.users.get(username).equals(password)) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                new VoteFrame().setVisible(true);
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
