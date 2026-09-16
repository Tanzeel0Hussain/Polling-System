package com.tanzeel.polling.ui;

import com.tanzeel.polling.model.User;
import com.tanzeel.polling.service.PollService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class AdminDashboardFrame extends JFrame {
    private final User user;
    private final PollService pollService = new PollService();
    private final DefaultListModel<PollService.Election> electionModel = new DefaultListModel<>();
    private final JList<PollService.Election> electionList = new JList<>(electionModel);
    private final JLabel usersStat = statValue("0");
    private final JLabel pollsStat = statValue("0");
    private final JLabel votesStat = statValue("0");
    private final JLabel pollTitle = Theme.heading("Select a poll", 22);
    private final JTextArea descriptionArea = new JTextArea();
    private final JLabel statusLabel = Theme.muted("Choose a poll to view results.");
    private final JButton toggleButton = Theme.primaryButton("Close Poll");
    private final DefaultTableModel resultModel = new DefaultTableModel(new Object[]{"Option", "Votes", "Share"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable resultTable = new JTable(resultModel);

    public AdminDashboardFrame(User user) {
        this.user = user;
        Theme.prepareFrame(this, "Polling System — Administrator", 1100, 720);
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        electionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        electionList.setFixedCellHeight(42);
        electionList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) loadSelectedPoll();
        });
        toggleButton.addActionListener(event -> toggleSelectedPoll());
        refreshAll();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(Theme.NAVY);
        header.setBorder(new EmptyBorder(18, 26, 18, 26));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Administrator Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(Theme.font(Font.BOLD, 22));
        JLabel account = new JLabel("Signed in as " + user.username());
        account.setForeground(new Color(218, 230, 240));
        account.setFont(Theme.font(Font.PLAIN, 13));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(account);
        header.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton create = Theme.accentButton("Create Poll");
        create.addActionListener(event -> showCreatePollDialog());
        JButton refresh = Theme.secondaryButton("Refresh");
        refresh.addActionListener(event -> refreshAll());
        JButton logout = Theme.secondaryButton("Log Out");
        logout.addActionListener(event -> logout());
        actions.add(create);
        actions.add(refresh);
        actions.add(logout);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setBackground(Theme.BG);
        body.setBorder(new EmptyBorder(22, 22, 22, 22));
        body.add(buildStats(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setOpaque(false);

        JPanel listCard = Theme.card(new BorderLayout(0, 12));
        listCard.setPreferredSize(new Dimension(330, 500));
        listCard.add(Theme.heading("All Polls", 18), BorderLayout.NORTH);
        JScrollPane listScroll = new JScrollPane(electionList);
        listScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        listCard.add(listScroll, BorderLayout.CENTER);
        content.add(listCard, BorderLayout.WEST);

        JPanel details = Theme.card(new BorderLayout(0, 14));
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        pollTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setRows(2);
        descriptionArea.setForeground(Theme.MUTED);
        descriptionArea.setFont(Theme.font(Font.PLAIN, 13));
        copy.add(pollTitle);
        copy.add(Box.createVerticalStrut(6));
        copy.add(descriptionArea);
        copy.add(Box.createVerticalStrut(7));
        copy.add(statusLabel);
        top.add(copy, BorderLayout.CENTER);
        toggleButton.setEnabled(false);
        top.add(toggleButton, BorderLayout.EAST);
        details.add(top, BorderLayout.NORTH);

        resultTable.setRowHeight(34);
        resultTable.setFillsViewportHeight(true);
        resultTable.getTableHeader().setBackground(Theme.BLUE_SOFT);
        resultTable.getTableHeader().setForeground(Theme.NAVY);
        JScrollPane tableScroll = new JScrollPane(resultTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        details.add(tableScroll, BorderLayout.CENTER);
        content.add(details, BorderLayout.CENTER);

        body.add(content, BorderLayout.CENTER);
        return body;
    }

    private JComponent buildStats() {
        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(statCard("Registered Voters", usersStat));
        stats.add(statCard("Total Polls", pollsStat));
        stats.add(statCard("Votes Cast", votesStat));
        return stats;
    }

    private JPanel statCard(String label, JLabel value) {
        JPanel card = Theme.card(new BorderLayout());
        card.add(value, BorderLayout.CENTER);
        card.add(Theme.muted(label), BorderLayout.SOUTH);
        return card;
    }

    private static JLabel statValue(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.NAVY);
        label.setFont(Theme.font(Font.BOLD, 28));
        return label;
    }

    private void refreshAll() {
        PollService.Election selected = electionList.getSelectedValue();
        PollService.DashboardStats stats = pollService.dashboardStats();
        usersStat.setText(String.valueOf(stats.users()));
        pollsStat.setText(String.valueOf(stats.elections()));
        votesStat.setText(String.valueOf(stats.votes()));

        electionModel.clear();
        List<PollService.Election> elections = pollService.listElections(false);
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
        else clearDetails();
    }

    private void loadSelectedPoll() {
        PollService.Election election = electionList.getSelectedValue();
        resultModel.setRowCount(0);
        if (election == null) {
            clearDetails();
            return;
        }

        pollTitle.setText(election.title());
        descriptionArea.setText(election.description().isBlank() ? "No description provided." : election.description());
        boolean open = "OPEN".equals(election.status());
        statusLabel.setText(open ? "Voting is currently OPEN." : "Voting is CLOSED. Results remain available below.");
        statusLabel.setForeground(open ? Theme.SUCCESS : Theme.MUTED);
        toggleButton.setText(open ? "Close Poll" : "Reopen Poll");
        toggleButton.setEnabled(true);

        for (PollService.ResultRow row : pollService.results(election.id())) {
            resultModel.addRow(new Object[]{row.candidate(), row.votes(), String.format("%.1f%%", row.percentage())});
        }
    }

    private void toggleSelectedPoll() {
        PollService.Election election = electionList.getSelectedValue();
        if (election == null) return;
        boolean currentlyOpen = "OPEN".equals(election.status());
        String action = currentlyOpen ? "close" : "reopen";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action + " \"" + election.title() + "\"?",
                "Update poll status", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            pollService.setElectionStatus(election.id(), !currentlyOpen);
            refreshAll();
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Update error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCreatePollDialog() {
        JTextField titleField = new JTextField();
        JTextArea descriptionField = new JTextArea(3, 28);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        JTextArea candidatesField = new JTextArea(7, 28);
        candidatesField.setText("Option A\nOption B");
        candidatesField.setLineWrap(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Poll title"), gbc);
        gbc.gridy++;
        form.add(titleField, gbc);
        gbc.gridy++;
        form.add(new JLabel("Description"), gbc);
        gbc.gridy++;
        form.add(new JScrollPane(descriptionField), gbc);
        gbc.gridy++;
        form.add(new JLabel("Candidates / options — one per line"), gbc);
        gbc.gridy++;
        form.add(new JScrollPane(candidatesField), gbc);

        int option = JOptionPane.showConfirmDialog(this, form, "Create New Poll",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) return;

        List<String> candidates = Arrays.stream(candidatesField.getText().split("\\R"))
                .map(String::trim).filter(value -> !value.isBlank()).toList();
        try {
            PollService.Election created = pollService.createElection(titleField.getText(), descriptionField.getText(), candidates);
            JOptionPane.showMessageDialog(this, "Poll created and opened for voting.",
                    "Poll created", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
            for (int i = 0; i < electionModel.size(); i++) {
                if (electionModel.get(i).id() == created.id()) electionList.setSelectedIndex(i);
            }
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Could not create poll", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearDetails() {
        pollTitle.setText("No polls yet");
        descriptionArea.setText("Create your first poll to begin collecting votes.");
        statusLabel.setText("No poll selected.");
        statusLabel.setForeground(Theme.MUTED);
        toggleButton.setEnabled(false);
        resultModel.setRowCount(0);
    }

    private void logout() {
        new WelcomeFrame().setVisible(true);
        dispose();
    }
}
