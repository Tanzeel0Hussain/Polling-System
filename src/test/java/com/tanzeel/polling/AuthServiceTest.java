package com.tanzeel.polling;

import com.tanzeel.polling.data.Database;
import com.tanzeel.polling.model.User;
import com.tanzeel.polling.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    @TempDir Path tempDir;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        System.setProperty("polling.db.path", tempDir.resolve("auth-test.db").toString());
        Database.initialize();
        authService = new AuthService();
    }

    @Test
    void registersAndAuthenticatesUser() {
        User created = authService.registerUser("alice", "Secure123".toCharArray());
        assertEquals(User.Role.USER, created.role());
        assertTrue(authService.authenticate("alice", "Secure123".toCharArray(), User.Role.USER).isPresent());
        assertTrue(authService.authenticate("alice", "Wrong123".toCharArray(), User.Role.USER).isEmpty());
    }

    @Test
    void preventsDuplicateUsernamesIgnoringCase() {
        authService.registerUser("alice", "Secure123".toCharArray());
        assertThrows(IllegalArgumentException.class,
                () -> authService.registerUser("ALICE", "Another123".toCharArray()));
    }

    @Test
    void createsOnlyOneFirstAdministrator() {
        assertFalse(authService.hasAdmin());
        User admin = authService.createFirstAdmin("admin", "Admin123".toCharArray());
        assertEquals(User.Role.ADMIN, admin.role());
        assertTrue(authService.hasAdmin());
        assertThrows(IllegalStateException.class,
                () -> authService.createFirstAdmin("admin2", "Admin456".toCharArray()));
    }
}
