//package myproject;

import javax.swing.*;
import java.awt.*;

public class UserRegistrationFrame extends JFrame {
    public UserRegistrationFrame() {
        setTitle("User Registration");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton registerButton = new JButton("Register");

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (!username.isEmpty() && !password.isEmpty()) {
                PollingSystem.users.put(username, password);
                JOptionPane.showMessageDialog(this, "User Registered Successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!");
            }
        });

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(registerButton);
    }
}
