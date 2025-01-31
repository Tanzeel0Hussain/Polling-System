//package myproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VoteFrame extends JFrame {
    public VoteFrame() {
        setTitle("Vote");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(0, 1));

        ButtonGroup group = new ButtonGroup();
        JRadioButton option1 = new JRadioButton("Tanzeel Hussain");
        JRadioButton option2 = new JRadioButton("Muhammad Sohail");
        JRadioButton option3 = new JRadioButton("Muhammad Ali");

        group.add(option1);
        group.add(option2);
        group.add(option3);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (option1.isSelected()) {
                    registerVote("Tanzeel Hussain");
                } else if (option2.isSelected()) {
                    registerVote("Muhammad Sohail");
                } else if (option3.isSelected()) {
                    registerVote("Muhammad Ali");
                }
                JOptionPane.showMessageDialog(null, "Vote Submitted!");
                dispose();
            }
        });

        add(option1);
        add(option2);
        add(option3);
        add(submitButton);
    }

    private void registerVote(String option) {
        PollingSystem.votes.put(option, PollingSystem.votes.getOrDefault(option, 0) + 1);
    }
}
