//package myproject;

import javax.swing.*;
import java.awt.*;

public class ResultFrame extends JFrame {
    public ResultFrame() {
        setTitle("Voting Results");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(0, 1));

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        StringBuilder results = new StringBuilder("Voting Results:\n");
        
        for (String option : PollingSystem.votes.keySet()) {
            results.append(option).append(": ").append(PollingSystem.votes.get(option)).append(" votes\n");
        }

        resultArea.setText(results.toString());
        add(new JScrollPane(resultArea));
    }
}
