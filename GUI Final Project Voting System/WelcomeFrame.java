//package myproject;

import javax.swing.*;
import java.awt.*;

public class WelcomeFrame extends JFrame {
    public WelcomeFrame() {
        setTitle("Polling System");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        JButton adminButton = new JButton("Admin Login");
        adminButton.addActionListener(e -> new AdminLoginFrame().setVisible(true));

        JButton userRegisterButton = new JButton("User Registration");
        userRegisterButton.addActionListener(e -> new UserRegistrationFrame().setVisible(true));

        JButton userLoginButton = new JButton("User Login");
        userLoginButton.addActionListener(e -> new UserLoginFrame().setVisible(true));

        add(adminButton);
        add(userRegisterButton);
        add(userLoginButton);
    }
}
