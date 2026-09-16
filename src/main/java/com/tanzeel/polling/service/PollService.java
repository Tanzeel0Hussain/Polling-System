package com.tanzeel.polling.service;

import com.tanzeel.polling.data.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PollService {
    public record Election(long id, String title, String description, String status) {
        @Override public String toString() { return title + "  [" + status + "]"; }
    }

    public record Candidate(long id, long electionId, String name) {
        @Override public String toString() { return name; }
    }

    public record ResultRow(String candidate, int votes, double percentage) {}
    public record DashboardStats(int users, int elections, int votes) {}

    public List<Election> listElections(boolean openOnly) {
        String sql = "SELECT id, title, description, status FROM elections "
                + (openOnly ? "WHERE status='OPEN' " : "") + "ORDER BY id DESC";
        List<Election> elections = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                elections.add(new Election(result.getLong("id"), result.getString("title"),
                        result.getString("description"), result.getString("status")));
            }
            return elections;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load polls.", exception);
        }
    }

    public List<Candidate> listCandidates(long electionId) {
        String sql = "SELECT id, election_id, name FROM candidates WHERE election_id=? ORDER BY id";
        List<Candidate> candidates = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, electionId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    candidates.add(new Candidate(result.getLong("id"), result.getLong("election_id"), result.getString("name")));
                }
            }
            return candidates;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load candidates.", exception);
        }
    }

    public Election createElection(String title, String description, List<String> rawCandidates) {
        String cleanTitle = title == null ? "" : title.trim();
        String cleanDescription = description == null ? "" : description.trim();
        if (cleanTitle.length() < 3 || cleanTitle.length() > 100) {
            throw new IllegalArgumentException("Poll title must be between 3 and 100 characters.");
        }

        Set<String> uniqueNames = new LinkedHashSet<>();
        for (String raw : rawCandidates == null ? List.<String>of() : rawCandidates) {
            String name = raw == null ? "" : raw.trim();
            if (!name.isBlank()) uniqueNames.add(name);
        }
        if (uniqueNames.size() < 2) throw new IllegalArgumentException("Add at least two unique candidates/options.");
        if (uniqueNames.stream().anyMatch(name -> name.length() > 80)) {
            throw new IllegalArgumentException("Candidate/option names must be 80 characters or fewer.");
        }

        String electionSql = "INSERT INTO elections(title, description, status) VALUES(?, ?, 'OPEN')";
        String candidateSql = "INSERT INTO candidates(election_id, name) VALUES(?, ?)";

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long electionId;
                try (PreparedStatement statement = connection.prepareStatement(electionSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, cleanTitle);
                    statement.setString(2, cleanDescription);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No election ID returned.");
                        electionId = keys.getLong(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(candidateSql)) {
                    for (String name : uniqueNames) {
                        statement.setLong(1, electionId);
                        statement.setString(2, name);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
                return new Election(electionId, cleanTitle, cleanDescription, "OPEN");
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof IllegalArgumentException illegal) throw illegal;
                throw new IllegalStateException("Could not create the poll.", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not create the poll.", exception);
        }
    }

    public void setElectionStatus(long electionId, boolean open) {
        String sql = "UPDATE elections SET status=? WHERE id=?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, open ? "OPEN" : "CLOSED");
            statement.setLong(2, electionId);
            if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Poll not found.");
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not update poll status.", exception);
        }
    }

    public boolean hasVoted(long electionId, long userId) {
        String sql = "SELECT 1 FROM votes WHERE election_id=? AND user_id=? LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, electionId);
            statement.setLong(2, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not check voting status.", exception);
        }
    }

    public void castVote(long electionId, long userId, long candidateId) {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String electionSql = "SELECT status FROM elections WHERE id=?";
                try (PreparedStatement statement = connection.prepareStatement(electionSql)) {
                    statement.setLong(1, electionId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new IllegalArgumentException("Poll not found.");
                        if (!"OPEN".equals(result.getString("status"))) throw new IllegalStateException("This poll is closed.");
                    }
                }

                String candidateSql = "SELECT 1 FROM candidates WHERE id=? AND election_id=?";
                try (PreparedStatement statement = connection.prepareStatement(candidateSql)) {
                    statement.setLong(1, candidateId);
                    statement.setLong(2, electionId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new IllegalArgumentException("Selected option does not belong to this poll.");
                    }
                }

                String voteSql = "INSERT INTO votes(election_id, user_id, candidate_id) VALUES(?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(voteSql)) {
                    statement.setLong(1, electionId);
                    statement.setLong(2, userId);
                    statement.setLong(3, candidateId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("unique")) {
                    throw new IllegalStateException("You have already voted in this poll.");
                }
                throw exception;
            } catch (RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not submit the vote.", exception);
        }
    }

    public List<ResultRow> results(long electionId) {
        String sql = """
            SELECT c.name, COUNT(v.id) AS vote_count
            FROM candidates c
            LEFT JOIN votes v ON v.candidate_id = c.id
            WHERE c.election_id = ?
            GROUP BY c.id, c.name
            ORDER BY vote_count DESC, c.name COLLATE NOCASE
            """;
        List<ResultRow> raw = new ArrayList<>();
        int total = 0;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, electionId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int votes = result.getInt("vote_count");
                    total += votes;
                    raw.add(new ResultRow(result.getString("name"), votes, 0));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load results.", exception);
        }

        final int voteTotal = total;
        return raw.stream()
                .map(row -> new ResultRow(row.candidate(), row.votes(), voteTotal == 0 ? 0 : row.votes() * 100.0 / voteTotal))
                .toList();
    }

    public DashboardStats dashboardStats() {
        try (Connection connection = Database.getConnection()) {
            return new DashboardStats(count(connection, "SELECT COUNT(*) FROM users WHERE role='USER'"),
                    count(connection, "SELECT COUNT(*) FROM elections"),
                    count(connection, "SELECT COUNT(*) FROM votes"));
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load dashboard statistics.", exception);
        }
    }

    private int count(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
        }
    }
}
