package com.tanzeel.polling.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class Theme {
    public static final Color NAVY = new Color(22, 48, 79);
    public static final Color BLUE = new Color(32, 95, 145);
    public static final Color BLUE_SOFT = new Color(235, 244, 251);
    public static final Color ACCENT = new Color(239, 132, 54);
    public static final Color BG = new Color(245, 248, 251);
    public static final Color TEXT = new Color(36, 52, 68);
    public static final Color MUTED = new Color(101, 117, 133);
    public static final Color BORDER = new Color(218, 226, 234);
    public static final Color SUCCESS = new Color(31, 130, 96);
    public static final Color DANGER = new Color(184, 61, 61);

    private Theme() {}

    public static void apply() {
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.font", font(Font.PLAIN, 14));
        UIManager.put("PasswordField.font", font(Font.PLAIN, 14));
        UIManager.put("TextArea.font", font(Font.PLAIN, 14));
        UIManager.put("Button.font", font(Font.BOLD, 13));
        UIManager.put("Table.font", font(Font.PLAIN, 13));
        UIManager.put("TableHeader.font", font(Font.BOLD, 13));
        UIManager.put("List.font", font(Font.PLAIN, 14));
    }

    public static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }

    public static JButton primaryButton(String text) {
        return button(text, BLUE, Color.WHITE);
    }

    public static JButton accentButton(String text) {
        return button(text, ACCENT, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        return button(text, Color.WHITE, BLUE);
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.equals(Color.WHITE) ? BORDER : background, 1, true),
                new EmptyBorder(9, 16, 9, 16)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        return panel;
    }

    public static JLabel heading(String text, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(NAVY);
        label.setFont(font(Font.BOLD, size));
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(font(Font.PLAIN, 13));
        return label;
    }

    public static void prepareFrame(JFrame frame, String title, int width, int height) {
        frame.setTitle(title);
        frame.setSize(width, height);
        frame.setMinimumSize(new Dimension(Math.min(width, 760), Math.min(height, 520)));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().setBackground(BG);
    }
}
