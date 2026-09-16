package com.tanzeel.polling.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String DEFAULT_PATH = "data/polling.db";

    private Database() {}

    public static Connection getConnection() throws SQLException {
        String dbPath = System.getProperty("polling.db.path", DEFAULT_PATH);
        prepareParentDirectory(dbPath);
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    public static void initialize() {
        String schema = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE COLLATE NOCASE,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role IN ('ADMIN','USER')),
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            );

            CREATE TABLE IF NOT EXISTS elections (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                status TEXT NOT NULL DEFAULT 'OPEN' CHECK(status IN ('OPEN','CLOSED')),
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            );

            CREATE TABLE IF NOT EXISTS candidates (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                election_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                FOREIGN KEY(election_id) REFERENCES elections(id) ON DELETE CASCADE,
                UNIQUE(election_id, name COLLATE NOCASE)
            );

            CREATE TABLE IF NOT EXISTS votes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                election_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                candidate_id INTEGER NOT NULL,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(election_id) REFERENCES elections(id) ON DELETE CASCADE,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
                UNIQUE(election_id, user_id)
            );

            CREATE INDEX IF NOT EXISTS idx_candidates_election ON candidates(election_id);
            CREATE INDEX IF NOT EXISTS idx_votes_election ON votes(election_id);
            CREATE INDEX IF NOT EXISTS idx_votes_candidate ON votes(candidate_id);
            """;

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) {
                if (!sql.isBlank()) statement.execute(sql);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not initialize the polling database.", exception);
        }
    }

    private static void prepareParentDirectory(String dbPath) {
        if (":memory:".equals(dbPath)) return;
        Path path = Paths.get(dbPath).toAbsolutePath();
        Path parent = path.getParent();
        if (parent == null) return;
        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create the data directory.", exception);
        }
    }
}
