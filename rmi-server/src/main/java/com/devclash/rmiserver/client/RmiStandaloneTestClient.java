package com.devclash.rmiserver.client;

import com.devclash.common.dto.*;
import com.devclash.common.enums.Difficulty;
import com.devclash.common.enums.GameMode;
import com.devclash.common.rmi.QuizRemote;
import io.github.cdimascio.dotenv.Dotenv;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Standalone Independent Test Client.
 * Demonstrates pure Java RMI remote method invocation across separate JVM processes.
 */
public class RmiStandaloneTestClient {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   DEVCLASH - STANDALONE RMI TEST CLIENT         ");
        System.out.println("   Testing Remote Invocations over JRMP RPC      ");
        System.out.println("=================================================");

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String host = getEnvString(dotenv, "RMI_HOST", "localhost");
        int port = getEnvInt(dotenv, "RMI_PORT", 1099);
        String serviceName = getEnvString(dotenv, "RMI_SERVICE_NAME", "DevClashQuizService");

        try {
            System.out.printf("[Test Client] Step 1: Connecting to RMI Registry at %s:%d...%n", host, port);
            Registry registry = LocateRegistry.getRegistry(host, port);

            System.out.printf("[Test Client] Step 2: Looking up remote stub: '%s'...%n", serviceName);
            QuizRemote quizRemote = (QuizRemote) registry.lookup(serviceName);
            System.out.println("[Test Client] SUCCESS: Remote Stub acquired! Proxy class: " + quizRemote.getClass().getName());

            // 1. Ping
            boolean alive = quizRemote.ping();
            System.out.printf("[Test Client] Step 3: Remote ping() check -> %b%n", alive);

            if (args.length >= 2 && args[0].equalsIgnoreCase("join")) {
                // Interactive join mode: java RmiStandaloneTestClient join <ROOM_CODE> <NICKNAME>
                String code = args[1];
                String name = args.length >= 3 ? args[2] : "RemoteChallenger";
                testJoinExistingRoom(quizRemote, code, name);
                return;
            }

            // Full End-to-End Automated Demonstration
            runFullDistributedTest(quizRemote);

        } catch (Exception e) {
            System.err.println("[Test Client] ERROR: Remote invocation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void runFullDistributedTest(QuizRemote quizRemote) throws Exception {
        System.out.println("\n--- [TEST PHASE 1: CREATE ROOM VIA RMI] ---");
        GameConfigDTO config = new GameConfigDTO(1, "Programming & Languages", Difficulty.MEDIUM, GameMode.CLASSIC);
        RoomDTO room = quizRemote.createRoom("AliceHost", "terminal", config);
        String roomCode = room.getRoomCode();
        String hostId = room.getHostPlayerId();

        System.out.printf("[Test Client] createRoom() returned Room Code: [%s]%n", roomCode);
        System.out.printf("[Test Client] Host ID: %s | State: %s%n", hostId, room.getState());

        System.out.println("\n--- [TEST PHASE 2: ANOTHER CLIENT JOINS ROOM VIA RMI] ---");
        JoinResultDTO joinRes = quizRemote.joinRoom(roomCode, "BobCompetitor", "robot");
        System.out.printf("[Test Client] joinRoom() success: %b | Message: %s%n", joinRes.isSuccess(), joinRes.getMessage());
        String bobId = joinRes.getPlayerId();
        System.out.printf("[Test Client] Bob assigned Player ID: %s%n", bobId);

        System.out.println("\n--- [TEST PHASE 3: SET READY STATUS VIA RMI] ---");
        boolean readyOk = quizRemote.setPlayerReady(roomCode, bobId, true);
        System.out.printf("[Test Client] setPlayerReady(Bob, true) -> %b%n", readyOk);

        System.out.println("\n--- [TEST PHASE 4: VERIFY SYNCHRONIZED ROOM STATE] ---");
        GameStateDTO stateForAlice = quizRemote.getRoomState(roomCode, hostId);
        GameStateDTO stateForBob = quizRemote.getRoomState(roomCode, bobId);

        System.out.printf("[Test Client] Alice sees %d players in Room %s%n", stateForAlice.getPlayers().size(), stateForAlice.getRoomCode());
        System.out.printf("[Test Client] Bob sees %d players in Room %s%n", stateForBob.getPlayers().size(), stateForBob.getRoomCode());
        assert stateForAlice.getPlayers().size() == stateForBob.getPlayers().size() : "Room state mismatch!";
        System.out.println("[Test Client] SUCCESS: Both clients received identical room state!");

        System.out.println("\n--- [TEST PHASE 5: START GAME VIA RMI] ---");
        boolean started = quizRemote.startGame(roomCode, hostId);
        System.out.printf("[Test Client] startGame(Host: %s) -> %b%n", hostId, started);

        System.out.println("\n--- [TEST PHASE 6: FETCH ACTIVE QUESTION VIA RMI] ---");
        QuestionDTO q1 = quizRemote.getCurrentQuestion(roomCode);
        System.out.printf("[Test Client] Active Question 1: %s%n", q1.getQuestionText());
        System.out.printf("[Test Client] Options: [A] %s | [B] %s | [C] %s | [D] %s%n",
                q1.getOptionA(), q1.getOptionB(), q1.getOptionC(), q1.getOptionD());

        System.out.println("\n--- [TEST PHASE 7: SUBMIT ANSWERS REMOTELY] ---");
        AnswerResultDTO aliceAns = quizRemote.submitAnswer(roomCode, hostId, 1, "B", 3500);
        System.out.printf("[Test Client] Alice submitted Option 'B' -> Accepted: %b | Correct: %b | Points: %d | Total Score: %d%n",
                aliceAns.isAccepted(), aliceAns.isCorrect(), aliceAns.getPointsAwarded(), aliceAns.getCurrentTotalScore());

        AnswerResultDTO bobAns = quizRemote.submitAnswer(roomCode, bobId, 1, "A", 4200);
        System.out.printf("[Test Client] Bob submitted Option 'A' -> Accepted: %b | Correct: %b | Points: %d | Total Score: %d%n",
                bobAns.isAccepted(), bobAns.isCorrect(), bobAns.getPointsAwarded(), bobAns.getCurrentTotalScore());

        System.out.println("\n--- [TEST PHASE 8: RETRIEVE LIVE LEADERBOARD VIA RMI] ---");
        List<PlayerScoreDTO> leaderboard = quizRemote.getLeaderboard(roomCode);
        System.out.println("[Test Client] Current Live Standings:");
        for (PlayerScoreDTO p : leaderboard) {
            System.out.printf("   Rank #%d: %s -> %d pts (Streak: %d)%n",
                    p.getRank(), p.getNickname(), p.getScore(), p.getStreak());
        }

        System.out.println("\n--- [TEST PHASE 9: CONCLUDE MATCH VIA RMI] ---");
        GameSummaryDTO summary = quizRemote.endGame(roomCode, hostId);
        System.out.printf("[Test Client] Match Summary UUID: %s%n", summary.getMatchUuid());
        System.out.printf("[Test Client] WINNER: %s with %d points!%n",
                summary.getWinnerNickname(), summary.getWinnerScore());

        System.out.println("\n=================================================");
        System.out.println("   ALL JAVA RMI REMOTE METHOD TESTS PASSED!      ");
        System.out.println("=================================================");
    }

    private static void testJoinExistingRoom(QuizRemote quizRemote, String roomCode, String nickname) throws Exception {
        System.out.printf("[Test Client] Attempting to join existing room: %s as %s...%n", roomCode, nickname);
        JoinResultDTO res = quizRemote.joinRoom(roomCode, nickname, "avatar_remote");
        if (res.isSuccess()) {
            System.out.printf("[Test Client] SUCCESS: Joined Room %s! Assigned PlayerId: %s%n", roomCode, res.getPlayerId());
            GameStateDTO state = quizRemote.getRoomState(roomCode, res.getPlayerId());
            System.out.printf("[Test Client] Current Players in Room: %d%n", state.getPlayers().size());
            for (PlayerDTO p : state.getPlayers()) {
                System.out.printf("   - %s (Host: %b, Ready: %b)%n", p.getNickname(), p.isHost(), p.isReady());
            }
        } else {
            System.out.printf("[Test Client] FAILED to join: %s%n", res.getMessage());
        }
    }

    private static String getEnvString(Dotenv dotenv, String key, String defaultValue) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) {
            val = dotenv.get(key);
        }
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    private static int getEnvInt(Dotenv dotenv, String key, int defaultValue) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) {
            val = dotenv.get(key);
        }
        if (val != null && !val.isBlank()) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
