package com.tanzeel.polling.service;

import com.tanzeel.polling.data.Database;
import com.tanzeel.polling.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class AuthService {
    public boolean hasAdmin() {
        String sql = "SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            return result.next();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not check administrator status.", exception);
        }
    }

    public User createFirstAdmin(String username, char[] password) {
        if (hasAdmin()) throw new IllegalStateException("An administrator already exists.");
        return createUser(username, password, User.Role.ADMIN);
    }

    public User registerUser(String username, char[] password) {
        return createUser(username, password, User.Role.USER);
    }

    public Optional<User> authenticate(String username, char[] password, User.Role expectedRole) {
        String normalizedUsername = normalizeUsername(username);
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ? COLLATE NOCASE";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedUsername);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return Optional.empty();
                User.Role role = User.Role.valueOf(result.getString("role"));
                if (role != expectedRole) return Optional.empty();
                if (!BCrypt.checkpw(new String(password), result.getString("password_hash"))) return Optional.empty();
                return Optional.of(new User(result.getLong("id"), result.getString("username"), role));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not sign in.", exception);
        }
    }

    private User createUser(String username, char[] password, User.Role role) {
        String normalizedUsername = normalizeUsername(username);
        validatePassword(password);
        String passwordHash = BCrypt.hashpw(new String(password), BCrypt.gensalt(12));
        String sql = "INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, normalizedUsername);
            statement.setString(2, passwordHash);
            statement.setString(3, role.name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return new User(keys.getLong(1), normalizedUsername, role);
            }
            throw new IllegalStateException("User was created but no identifier was returned.");
        } catch (SQLException exception) {
            if (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("unique")) {
                throw new IllegalArgumentException("That username is already registered.");
            }
            throw new IllegalStateException("Could not create the account.", exception);
        }
    }

    private String normalizeUsername(String username) {
        String normalized = username == null ? "" : username.trim();
        if (normalized.length() < 3 || normalized.length() > 40) {
            throw new IllegalArgumentException("Username must be between 3 and 40 characters.");
        }
        if (!normalized.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("Username may only contain letters, numbers, dot, underscore and hyphen.");
        }
        return normalized;
    }

    private void validatePassword(char[] password) {
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
        String value = new String(password);
        if (!value.matches(".*[A-Za-z].*") || !value.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must include at least one letter and one number.");
        }
    }
}
