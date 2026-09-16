package com.tanzeel.polling;

import com.tanzeel.polling.data.Database;
import com.tanzeel.polling.model.User;
import com.tanzeel.polling.service.AuthService;
import com.tanzeel.polling.service.PollService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PollServiceTest {
    @TempDir Path tempDir;
    private PollService pollService;
    private User voter;

    @BeforeEach
    void setUp() {
        System.setProperty("polling.db.path", tempDir.resolve("poll-test.db").toString());
        Database.initialize();
        AuthService authService = new AuthService();
        voter = authService.registerUser("voter1", "Secure123".toCharArray());
        pollService = new PollService();
    }

    @Test
    void createsPollCastsVoteAndCalculatesResults() {
        PollService.Election election = pollService.createElection(
                "Best project idea", "Choose one option", List.of("Option A", "Option B"));
        List<PollService.Candidate> candidates = pollService.listCandidates(election.id());
        assertEquals(2, candidates.size());

        pollService.castVote(election.id(), voter.id(), candidates.getFirst().id());
        assertTrue(pollService.hasVoted(election.id(), voter.id()));
        assertThrows(IllegalStateException.class,
                () -> pollService.castVote(election.id(), voter.id(), candidates.getLast().id()));

        List<PollService.ResultRow> results = pollService.results(election.id());
        assertEquals(1, results.stream().mapToInt(PollService.ResultRow::votes).sum());
        assertEquals(100.0, results.stream().mapToDouble(PollService.ResultRow::percentage).sum(), 0.001);
    }

    @Test
    void closedPollRejectsVotes() {
        PollService.Election election = pollService.createElection(
                "Closed poll", "Testing status", List.of("Yes", "No"));
        pollService.setElectionStatus(election.id(), false);
        long candidateId = pollService.listCandidates(election.id()).getFirst().id();
        assertThrows(IllegalStateException.class,
                () -> pollService.castVote(election.id(), voter.id(), candidateId));
    }
}
