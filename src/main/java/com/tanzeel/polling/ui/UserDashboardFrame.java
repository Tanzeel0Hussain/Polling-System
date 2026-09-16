package com.tanzeel.polling.ui;

import com.tanzeel.polling.model.User;
import com.tanzeel.polling.service.PollService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserDashboardFrame extends JFrame {
    private final User user;
    private final PollService pollService = new PollService();
    private final DefaultListModel<PollService.Election> electionModel = new DefaultListModel<>();
    private final JList<PollService.Election> electionList = new JList<>(electionModel);
    private final JPanel candidatePanel = new JPanel();
    private final JLabel pollTitle = Theme.heading("Select an open poll", 22);
    private final JTextArea descriptionArea = new JTextArea();
    private final JLabel statusLabel = Theme.muted("Choose a poll from the list to view its options.");
    private final JButton voteButton = Theme.accentButton("Submit Vote");
    private final ButtonGroup candidateGroup = new ButtonGroup();
    private final Map<AbstractButton, PollService.Candidate> candidateMap = new LinkedHashMap<>();

    public UserDashboardFrame(User user) {
        this.user = user;
        Theme.prepareFrame(this, "Polling System — Voter Dashboard", 980, 650);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        electionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        electionList.setFixedCellHeight(42);
        electionList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) loadSelectedPoll();
        });
        voteButton.addActionListener(event -> submitVote());
        refreshPolls();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(Theme.NAVY);
        header.setBorder(new EmptyBorder(18, 26, 18, 26));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Voter Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(Theme.font(Font.BOLD, 22));
        JLabel welcome = new JLabel("Signed in as " + user.username());
        welcome.setForeground(new Color(218, 230, 240));
        welcome.setFont(Theme.font(Font.PLAIN, 13));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(welcome);
        header.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton refresh = Theme.secondaryButton("Refresh");
        refresh.addActionListener(event -> refreshPolls());
        JButton logout = Theme.accentButton("Log Out");
        logout.addActionListener(event -> logout());
        actions.add(refresh);
        actions.add(logout);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JComponent buildContent() {
        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setBackground(Theme.BG);
        body.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel listCard = Theme.card(new BorderLayout(0, 12));
        listCard.setPreferredSize(new Dimension(320, 500));
        listCard.add(Theme.heading("Open Polls", 18), BorderLayout.NORTH);
        JScrollPane electionScroll = new JScrollPane(electionList);
        electionScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        listCard.add(electionScroll, BorderLayout.CENTER);
        body.add(listCard, BorderLayout.WEST);

        JPanel detailCard = Theme.card(new BorderLayout(0, 16));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        pollTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setForeground(Theme.MUTED);
        descriptionArea.setFont(Theme.font(Font.PLAIN, 13));
        descriptionArea.setRows(3);
        top.add(pollTitle);
        top.add(Box.createVerticalStrut(7));
        top.add(descriptionArea);
        top.add(Box.createVerticalStrut(8));
        top.add(statusLabel);
        detailCard.add(top, BorderLayout.NORTH);

        candidatePanel.setOpaque(false);
        candidatePanel.setLayout(new BoxLayout(candidatePanel, BoxLayout.Y_AXIS));
        JScrollPane optionsScroll = new JScrollPane(candidatePanel);
        optionsScroll.setBorder(null);
        optionsScroll.getViewport().setBackground(Color.WHITE);
        detailCard.add(optionsScroll, BorderLayout.CENTER);

        voteButton.setEnabled(false);
        detailCard.add(voteButton, BorderLayout.SOUTH);
        body.add(detailCard, BorderLayout.CENTER);
        return body;
    }

    private void refreshPolls() {
        PollService.Election selected = electionList.getSelectedValue();
        electionModel.clear();
        List<PollService.Election> elections = pollService.listElections(true);
        elections.forEach(electionModel::addElement);
        if (selected != null) {
            for (int i = 0; i < electionModel.size(); i++) {
                if (electionModel.get(i).id() == selected.id()) {
                    electionList.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (!elections.isEmpty()) electionList.setSelectedIndex(0);
        else clearDetails("There are no open polls right now.");
    }

    private void loadSelectedPoll() {
        PollService.Election election = electionList.getSelectedValue();
        if (election == null) {
            clearDetails("Select a poll to continue.");
            return;
        }

        pollTitle.setText(election.title());
        descriptionArea.setText(election.description().isBlank() ? "No description provided." : election.description());
        candidatePanel.removeAll();
        candidateMap.clear();
        candidateGroup.clearSelection();

        boolean alreadyVoted = pollService.hasVoted(election.id(), user.id());
        List<PollService.Candidate> candidates = pollService.listCandidates(election.id());
        for (PollService.Candidate candidate : candidates) {
            JRadioButton radio = new JRadioButton(candidate.name());
            radio.setOpaque(false);
            radio.setFont(Theme.font(Font.PLAIN, 15));
            radio.setForeground(Theme.TEXT);
            radio.setBorder(new EmptyBorder(10, 6, 10, 6));
            radio.setEnabled(!alreadyVoted);
            candidateGroup.add(radio);
            candidateMap.put(radio, candidate);
            candidatePanel.add(radio);
            candidatePanel.add(new JSeparator());
        }

        if (alreadyVoted) {
            statusLabel.setText("Vote recorded — this account has already voted in this poll.");
            statusLabel.setForeground(Theme.SUCCESS);
            voteButton.setEnabled(false);
        } else {
            statusLabel.setText("Select one option. A submitted vote cannot be changed.");
            statusLabel.setForeground(Theme.MUTED);
            voteButton.setEnabled(!candidates.isEmpty());
        }
        candidatePanel.revalidate();
        candidatePanel.repaint();
    }

    private void submitVote() {
        PollService.Election election = electionList.getSelectedValue();
        if (election == null) return;
        PollService.Candidate selected = candidateMap.entrySet().stream()
                .filter(entry -> entry.getKey().isSelected())
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an option before submitting your vote.",
                    "No option selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Submit your vote for \"" + selected.name() + "\"?\nThis action cannot be changed.",
                "Confirm vote", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            pollService.castVote(election.id(), user.id(), selected.id());
            JOptionPane.showMessageDialog(this, "Your vote has been recorded successfully.",
                    "Vote submitted", JOptionPane.INFORMATION_MESSAGE);
            loadSelectedPoll();
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Voting error", JOptionPane.ERROR_MESSAGE);
            refreshPolls();
        }
    }

    private void clearDetails(String message) {
        pollTitle.setText("No poll selected");
        descriptionArea.setText("");
        statusLabel.setText(message);
        statusLabel.setForeground(Theme.MUTED);
        candidatePanel.removeAll();
        candidatePanel.revalidate();
        candidatePanel.repaint();
        voteButton.setEnabled(false);
    }

    private void logout() {
        new WelcomeFrame().setVisible(true);
        dispose();
    }
}
