//package myproject;

import javax.swing.*;
import java.awt.*;

public class RegistrationFrame extends JFrame {
    public RegistrationFrame() {
        setTitle("User Registration");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(0, 1));

        JTextField nameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton registerButton = new JButton("Register");

        registerButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "User Registered Successfully!");
            dispose();
        });

        add(new JLabel("Username:"));
        add(nameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(registerButton);
    }
}
