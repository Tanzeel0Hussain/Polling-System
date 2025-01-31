//package myproject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class PollingSystem {
    public static HashMap<String, Integer> votes = new HashMap<>();
    public static HashMap<String, String> users = new HashMap<>();
    public static String adminUsername = "admin@gmail.com";
    public static String adminPassword = "admin";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomeFrame().setVisible(true));
    }
}
