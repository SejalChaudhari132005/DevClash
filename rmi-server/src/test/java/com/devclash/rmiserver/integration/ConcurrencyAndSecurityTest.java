package com.devclash.rmiserver.integration;

import com.devclash.common.dto.*;
import com.devclash.common.enums.Difficulty;
import com.devclash.common.enums.GameMode;
import com.devclash.rmiserver.service.QuizRemoteImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated Concurrency, Failure, and Security Integration Test Suite for DevClash.
 * Tests RMI state machine, multi-client concurrent operations, tie-breaking, authorization, and resilience.
 */
public class ConcurrencyAndSecurityTest {

    private QuizRemoteImpl quizService;

    @BeforeEach
    void setUp() throws RemoteException {
        quizService = new QuizRemoteImpl();
    }

    private GameConfigDTO createTestConfig(int totalQuestions) {
        GameConfigDTO config = new GameConfigDTO(1, "PROGRAMMING", Difficulty.MEDIUM, GameMode.CLASSIC);
        config.setTotalQuestions(totalQuestions);
        config.setQuestionTimeLimitSec(15);
        return config;
    }

    // =========================================================================
    // CONCURRENCY TESTS
    // =========================================================================

    @Test
    @DisplayName("Concurrency 1: Multiple players join simultaneously")
    void testSimultaneousPlayerJoins() throws Exception {
        RoomDTO room = quizService.createRoom("HostAlice", "terminal", createTestConfig(2));
        String roomCode = room.getRoomCode();

        int joiners = 8;
        ExecutorService executor = Executors.newFixedThreadPool(joiners);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(joiners);
        List<JoinResultDTO> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= joiners; i++) {
            final String nickname = "Player_" + i;
            executor.submit(() -> {
                try {
                    startGate.await();
                    JoinResultDTO res = quizService.joinRoom(roomCode, nickname, "wizard");
                    results.add(res);
                } catch (Exception e) {
                    results.add(JoinResultDTO.fail(e.getMessage()));
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        assertTrue(endGate.await(5, TimeUnit.SECONDS), "All joins should complete within 5 seconds");
        executor.shutdown();

        int successfulJoins = 0;
        for (JoinResultDTO r : results) {
            if (r.isSuccess()) {
                successfulJoins++;
            } else {
                System.err.println(">>> JOIN FAILURE: " + r.getMessage());
            }
        }
        System.out.println("Total results recorded: " + results.size() + ", successful: " + successfulJoins);
        assertEquals(8, successfulJoins, "All 8 concurrent players must successfully join");

        RoomDTO updatedRoom = quizService.getRoom(roomCode);
        assertEquals(9, updatedRoom.getPlayers().size(), "Room must have 1 host + 8 players = 9 players");
    }

    @Test
    @DisplayName("Concurrency 2: Multiple players submit answers at the same millisecond")
    void testSimultaneousAnswerSubmissions() throws Exception {
        RoomDTO room = quizService.createRoom("HostAlice", "terminal", createTestConfig(2));
        String roomCode = room.getRoomCode();

        JoinResultDTO bobJoin = quizService.joinRoom(roomCode, "Bob", "wizard");
        JoinResultDTO charlieJoin = quizService.joinRoom(roomCode, "Charlie", "knight");

        quizService.setPlayerReady(roomCode, bobJoin.getPlayerId(), true);
        quizService.setPlayerReady(roomCode, charlieJoin.getPlayerId(), true);

        // Start match
        quizService.startGame(roomCode, room.getHostPlayerId());
        Thread.sleep(3500); // Wait for countdown to enter Question 1

        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(3);
        List<AnswerResultDTO> answers = Collections.synchronizedList(new ArrayList<>());

        String[] playerIds = {room.getHostPlayerId(), bobJoin.getPlayerId(), charlieJoin.getPlayerId()};
        String[] options = {"A", "B", "C"};

        for (int i = 0; i < 3; i++) {
            final String pid = playerIds[i];
            final String opt = options[i];
            executor.submit(() -> {
                try {
                    latch.await();
                    AnswerResultDTO res = quizService.submitAnswer(roomCode, pid, 1, opt, 1200);
                    answers.add(res);
                } catch (Exception e) {
                    answers.add(AnswerResultDTO.rejected(e.getMessage()));
                } finally {
                    done.countDown();
                }
            });
        }

        latch.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(3, answers.size());
        for (AnswerResultDTO a : answers) {
            assertTrue(a.isAccepted(), "All unique simultaneous submissions must be accepted");
        }
    }

    @Test
    @DisplayName("Concurrency 3: Duplicate submission attempt must be rejected")
    void testDuplicateSubmissionRejection() throws Exception {
        RoomDTO room = quizService.createRoom("HostAlice", "terminal", createTestConfig(2));
        String roomCode = room.getRoomCode();
        JoinResultDTO bob = quizService.joinRoom(roomCode, "Bob", "wizard");
        quizService.setPlayerReady(roomCode, bob.getPlayerId(), true);
        quizService.startGame(roomCode, room.getHostPlayerId());
        Thread.sleep(3500);

        // First answer
        AnswerResultDTO first = quizService.submitAnswer(roomCode, bob.getPlayerId(), 1, "B", 1500);
        assertTrue(first.isAccepted(), "First answer submission must be accepted");

        // Duplicate answer in same round
        AnswerResultDTO second = quizService.submitAnswer(roomCode, bob.getPlayerId(), 1, "C", 2000);
        assertFalse(second.isAccepted(), "Duplicate answer submission in the same round must be rejected");
        assertTrue(second.getStatusMessage().toLowerCase().contains("already submitted"),
                "Rejection message should indicate answer was already submitted");
    }

    @Test
    @DisplayName("Concurrency 4: Submission after timer expires must be rejected")
    void testSubmissionAfterExpiry() throws Exception {
        RoomDTO room = quizService.createRoom("HostAlice", "terminal", createTestConfig(2));
        String roomCode = room.getRoomCode();
        JoinResultDTO bob = quizService.joinRoom(roomCode, "Bob", "wizard");
        quizService.setPlayerReady(roomCode, bob.getPlayerId(), true);
        quizService.startGame(roomCode, room.getHostPlayerId());
        Thread.sleep(3500);

        // Alice and Bob both answer to trigger early conclusion
        quizService.submitAnswer(roomCode, room.getHostPlayerId(), 1, "A", 1000);
        quizService.submitAnswer(roomCode, bob.getPlayerId(), 1, "B", 1200);
        Thread.sleep(500); // Round is now QUESTION_RESULT

        // Attempt submission after round has closed
        AnswerResultDTO late = quizService.submitAnswer(roomCode, bob.getPlayerId(), 1, "B", 3000);
        assertFalse(late.isAccepted(), "Submission after round has concluded must be rejected");
    }

    @Test
    @DisplayName("Concurrency 5: Multiple rooms run simultaneously without interference")
    void testMultipleRoomsSimultaneously() throws Exception {
        RoomDTO room1 = quizService.createRoom("Alice", "terminal", createTestConfig(2));
        RoomDTO room2 = quizService.createRoom("Bob", "wizard", createTestConfig(2));

        assertNotEquals(room1.getRoomCode(), room2.getRoomCode(), "Room codes must be unique");

        JoinResultDTO charlieJoin = quizService.joinRoom(room1.getRoomCode(), "Charlie", "knight");
        JoinResultDTO daveJoin = quizService.joinRoom(room2.getRoomCode(), "Dave", "ninja");

        quizService.setPlayerReady(room1.getRoomCode(), charlieJoin.getPlayerId(), true);
        quizService.setPlayerReady(room2.getRoomCode(), daveJoin.getPlayerId(), true);

        // Start both matches
        assertTrue(quizService.startGame(room1.getRoomCode(), room1.getHostPlayerId()));
        assertTrue(quizService.startGame(room2.getRoomCode(), room2.getHostPlayerId()));

        Thread.sleep(3500);

        // Verify independent question states
        GameStateDTO state1 = quizService.getRoomState(room1.getRoomCode(), room1.getHostPlayerId());
        GameStateDTO state2 = quizService.getRoomState(room2.getRoomCode(), room2.getHostPlayerId());

        assertNotNull(state1.getCurrentQuestion(), "Room 1 must have active question");
        assertNotNull(state2.getCurrentQuestion(), "Room 2 must have active question");
        assertEquals(room1.getRoomCode(), state1.getRoomCode());
        assertEquals(room2.getRoomCode(), state2.getRoomCode());
    }

    // =========================================================================
    // FAILURE & VALIDATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Failure 1: Invalid room code rejection")
    void testInvalidRoomCode() throws RemoteException {
        JoinResultDTO res = quizService.joinRoom("INVALID", "Tester", "terminal");
        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().contains("not found"));

        JoinResultDTO resEmpty = quizService.joinRoom("", "Tester", "terminal");
        assertFalse(resEmpty.isSuccess());
    }

    @Test
    @DisplayName("Failure 2: Duplicate nickname rejection")
    void testDuplicateNicknameRejection() throws RemoteException {
        RoomDTO room = quizService.createRoom("Alice", "terminal", createTestConfig(2));
        JoinResultDTO res = quizService.joinRoom(room.getRoomCode(), "ALICE", "wizard");
        assertFalse(res.isSuccess(), "Case-insensitive duplicate nickname must be rejected");
        assertTrue(res.getMessage().contains("already taken"));
    }

    @Test
    @DisplayName("Failure 3: Host departure promotes next player to host")
    void testHostDeparturePromotion() throws RemoteException {
        RoomDTO room = quizService.createRoom("HostAlice", "terminal", createTestConfig(2));
        String roomCode = room.getRoomCode();
        String hostId = room.getHostPlayerId();

        JoinResultDTO bob = quizService.joinRoom(roomCode, "Bob", "wizard");
        JoinResultDTO charlie = quizService.joinRoom(roomCode, "Charlie", "knight");

        // Host leaves the room
        boolean left = quizService.leaveRoom(roomCode, hostId);
        assertTrue(left);

        RoomDTO updated = quizService.getRoom(roomCode);
        assertNotNull(updated);
        assertEquals(bob.getPlayerId(), updated.getHostPlayerId(), "Bob must be promoted to new Host");
        assertTrue(updated.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(bob.getPlayerId()) && p.isHost()));
    }

    @Test
    @DisplayName("Failure 4: Player disconnect and reconnect tracking")
    void testPlayerDisconnectAndReconnect() throws RemoteException {
        RoomDTO room = quizService.createRoom("HostAlice", "terminal", createTestConfig(2));
        String roomCode = room.getRoomCode();
        JoinResultDTO bob = quizService.joinRoom(roomCode, "Bob", "wizard");

        // Player disconnects
        quizService.setPlayerConnected(roomCode, bob.getPlayerId(), false);
        RoomDTO roomDisc = quizService.getRoom(roomCode);
        PlayerDTO bobDisc = roomDisc.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(bob.getPlayerId())).findFirst().orElse(null);
        assertNotNull(bobDisc);
        assertFalse(bobDisc.isConnected(), "Bob must be marked as disconnected");

        // Player reconnects
        quizService.setPlayerConnected(roomCode, bob.getPlayerId(), true);
        RoomDTO roomReconn = quizService.getRoom(roomCode);
        PlayerDTO bobReconn = roomReconn.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(bob.getPlayerId())).findFirst().orElse(null);
        assertNotNull(bobReconn);
        assertTrue(bobReconn.isConnected(), "Bob must be marked as connected again");
    }

    // =========================================================================
    // SECURITY TESTS
    // =========================================================================

    @Test
    @DisplayName("Security 1: Restrict host-only actions from non-host")
    void testRestrictHostOnlyActions() throws RemoteException {
        RoomDTO room = quizService.createRoom("HostAlice", "terminal", createTestConfig(2));
        String roomCode = room.getRoomCode();
        JoinResultDTO bob = quizService.joinRoom(roomCode, "Bob", "wizard");
        quizService.setPlayerReady(roomCode, bob.getPlayerId(), true);

        // Bob attempts to start game (Non-host)
        assertThrows(RemoteException.class, () -> {
            quizService.startGame(roomCode, bob.getPlayerId());
        }, "Non-host starting match must throw RemoteException");

        // Bob attempts to kick HostAlice
        assertThrows(RemoteException.class, () -> {
            quizService.removePlayer(roomCode, bob.getPlayerId(), room.getHostPlayerId());
        }, "Non-host kicking another player must throw RemoteException");
    }

    @Test
    @DisplayName("Security 2: Validate answer option and sanitize nicknames")
    void testInputValidationAndSanitization() throws RemoteException {
        // Nickname with HTML tags
        RoomDTO room = quizService.createRoom("<script>alert('xss')</script>Alice", "terminal", createTestConfig(2));
        assertFalse(room.getHostNickname().contains("<script>"), "HTML tags must be stripped from nickname");

        // Invalid answer option
        AnswerResultDTO invalidOpt = quizService.submitAnswer(room.getRoomCode(), room.getHostPlayerId(), 1, "Z", 1000);
        assertFalse(invalidOpt.isAccepted(), "Option other than A, B, C, D must be rejected");
    }
}
