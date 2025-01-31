//package myproject;

import javax.swing.*;
import java.awt.*;

public class AdminPanelFrame extends JFrame {
    public AdminPanelFrame() {
        setTitle("Admin Panel");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());

        JButton viewResultsButton = new JButton("View Results");
        viewResultsButton.addActionListener(e -> new ResultFrame().setVisible(true));

        add(viewResultsButton);
    }
}
