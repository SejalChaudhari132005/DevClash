package com.devclash.rmiserver.service;

import com.devclash.common.dto.*;
import com.devclash.common.enums.Difficulty;
import com.devclash.common.enums.GameMode;
import com.devclash.common.enums.RoomState;
import com.devclash.common.rmi.GameCallbackListener;
import com.devclash.common.rmi.QuizRemote;
import com.devclash.rmiserver.config.DatabaseConfig;
import com.devclash.rmiserver.engine.FullQuestion;
import com.devclash.rmiserver.engine.ScoringResult;
import com.devclash.rmiserver.engine.TechBattleQuizEngine;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Core Implementation of the QuizRemote interface.
 * Extends UnicastRemoteObject to enable remote invocation across JVM process boundaries.
 * Backed by TechBattleQuizEngine for server-side evaluation, scoring, and question selection.
 * Serves as the authoritative distributed state machine:
 * WAITING -> COUNTDOWN -> QUESTION_ACTIVE -> QUESTION_RESULT -> NEXT_QUESTION -> FINISHED.
 */
public class QuizRemoteImpl extends UnicastRemoteObject implements QuizRemote {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, ActiveGameRoom> rooms = new ConcurrentHashMap<>();
    private final Map<String, GameCallbackListener> listeners = new ConcurrentHashMap<>();
    private final ScheduledExecutorService globalScheduler = Executors.newScheduledThreadPool(4);

    public QuizRemoteImpl() throws RemoteException {
        this(0);
    }

    public QuizRemoteImpl(int exportPort) throws RemoteException {
        super(exportPort);
    }

    @Override
    public void registerCallbackListener(String clientKey, GameCallbackListener listener) throws RemoteException {
        if (clientKey != null && listener != null) {
            listeners.put(clientKey, listener);
            System.out.printf("[RMI Server] Registered remote callback listener '%s' (Total: %d)%n",
                    clientKey, listeners.size());
        }
    }

    @Override
    public void unregisterCallbackListener(String clientKey) throws RemoteException {
        if (clientKey != null) {
            listeners.remove(clientKey);
            System.out.printf("[RMI Server] Unregistered remote callback listener '%s'%n", clientKey);
        }
    }

    private void notifyListeners(Consumer<GameCallbackListener> action) {
        if (listeners.isEmpty()) return;
        List<String> failedKeys = new ArrayList<>();
        for (Map.Entry<String, GameCallbackListener> entry : listeners.entrySet()) {
            try {
                action.accept(entry.getValue());
            } catch (Exception e) {
                System.err.printf("[RMI Server] Callback to '%s' failed: %s%n", entry.getKey(), e.getMessage());
                failedKeys.add(entry.getKey());
            }
        }
        for (String key : failedKeys) {
            listeners.remove(key);
        }
    }

    @Override
    public RoomDTO createRoom(String hostNickname, String hostAvatar, GameConfigDTO config) throws RemoteException {
        if (hostNickname == null || hostNickname.trim().isEmpty()) {
            hostNickname = "HostPlayer";
        }
        hostNickname = hostNickname.trim().replaceAll("<[^>]*>", "");
        if (hostNickname.length() > 30) {
            hostNickname = hostNickname.substring(0, 30);
        }
        if (config == null) {
            config = new GameConfigDTO(1, "Programming & Languages", Difficulty.MEDIUM, GameMode.CLASSIC);
        }

        String roomCode = generateUniqueRoomCode();
        String hostId = "p_" + UUID.randomUUID().toString().substring(0, 8);
        String hostToken = UUID.randomUUID().toString();

        // Select questions without repetition via TechBattleQuizEngine
        List<FullQuestion> questions = TechBattleQuizEngine.selectQuestions(
                config.getGameMode(),
                config.getCategoryId(),
                config.getDifficulty(),
                config.getTotalQuestions()
        );

        ActiveGameRoom room = new ActiveGameRoom(roomCode, hostId, hostNickname.trim(), hostAvatar, hostToken, config, questions);
        rooms.put(roomCode, room);

        System.out.println("================================================================================");
        System.out.printf("[RMI Server] Room Created: %s | Host: %s (%s)%n", roomCode, hostNickname, hostId);
        System.out.printf("[CONFIG] Category: %s | Mode: %s | Difficulty: %s | Questions: %d%n",
                config.getCategoryName(), config.getGameMode(), config.getDifficulty(), questions.size());
        System.out.println("================================================================================");

        RoomDTO roomDTO = room.toRoomDTO();
        roomDTO.setHostSessionToken(hostToken);
        return roomDTO;
    }

    @Override
    public JoinResultDTO joinRoom(String roomCode, String playerNickname, String playerAvatar) throws RemoteException {
        if (roomCode == null || roomCode.trim().isEmpty()) {
            return JoinResultDTO.fail("Room code cannot be empty.");
        }
        roomCode = roomCode.trim().toUpperCase();
        ActiveGameRoom room = rooms.get(roomCode);
        if (room == null) {
            return JoinResultDTO.fail("Room " + roomCode + " not found. Please check the code.");
        }

        if (room.state != RoomState.WAITING && room.state != RoomState.WAITING_LOBBY) {
            return JoinResultDTO.fail("Cannot join: Battle in room " + roomCode + " has already started.");
        }

        if (playerNickname == null || playerNickname.trim().isEmpty()) {
            playerNickname = "Player_" + (room.players.size() + 1);
        }
        playerNickname = playerNickname.trim().replaceAll("<[^>]*>", "");
        if (playerNickname.length() > 30) {
            playerNickname = playerNickname.substring(0, 30);
        }

        String playerId = "p_" + UUID.randomUUID().toString().substring(0, 8);
        String sessionToken = UUID.randomUUID().toString();

        PlayerDTO player = new PlayerDTO(playerId, playerNickname, false, playerAvatar);
        int addResult = room.addPlayer(player, sessionToken);
        if (addResult == 1) {
            return JoinResultDTO.fail("Cannot join: Room " + roomCode + " is already full (maximum 10 players allowed).");
        }
        if (addResult == 2) {
            return JoinResultDTO.fail("Nickname '" + playerNickname + "' is already taken in this room. Please choose another nickname.");
        }

        System.out.printf("[RMI Server] Player Joined: %s (%s) into Room: %s | Total: %d/10%n",
                playerNickname, playerId, roomCode, room.players.size());

        notifyListeners(l -> {
            try {
                l.onPlayerJoined(room.roomCode, player);
            } catch (RemoteException ignored) {}
        });

        return JoinResultDTO.ok(playerId, sessionToken, room.toRoomDTO());
    }

    @Override
    public boolean leaveRoom(String roomCode, String playerId) throws RemoteException {
        if (roomCode == null || playerId == null) return false;
        ActiveGameRoom room = rooms.get(roomCode.trim().toUpperCase());
        if (room == null) return false;

        boolean removed = room.removePlayer(playerId);
        System.out.printf("[RMI Server] Player %s left Room %s%n", playerId, roomCode);

        notifyListeners(l -> {
            try {
                l.onPlayerLeft(roomCode, playerId, "Voluntarily left the room");
            } catch (RemoteException ignored) {}
        });

        if (room.isEmpty()) {
            room.cleanup();
            rooms.remove(roomCode.trim().toUpperCase());
            System.out.printf("[RMI Server] Room %s removed (no remaining players)%n", roomCode);
        }
        return removed;
    }

    @Override
    public boolean removePlayer(String roomCode, String hostPlayerId, String targetPlayerId) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) return false;
        if (!room.hostId.equals(hostPlayerId)) {
            throw new RemoteException("Access denied: Only the room host can remove players.");
        }
        if (room.hostId.equals(targetPlayerId)) {
            throw new RemoteException("Host cannot remove themselves from the room.");
        }
        boolean removed = room.removePlayer(targetPlayerId);
        System.out.printf("[RMI Server] Host %s removed player %s from Room %s%n",
                hostPlayerId, targetPlayerId, roomCode);

        notifyListeners(l -> {
            try {
                l.onPlayerLeft(roomCode, targetPlayerId, "Removed by host");
            } catch (RemoteException ignored) {}
        });

        return removed;
    }

    @Override
    public boolean setPlayerConnected(String roomCode, String playerId, boolean connected) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) return false;
        room.setPlayerConnected(playerId, connected);

        System.out.printf("[RMI Server] Player %s in Room %s connection status: %b%n",
                playerId, roomCode, connected);

        return true;
    }

    @Override
    public RoomDTO getRoom(String roomCode) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) return null;
        return room.toRoomDTO();
    }

    @Override
    public boolean setPlayerReady(String roomCode, String playerId, boolean ready) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) return false;
        room.setPlayerReady(playerId, ready);

        notifyListeners(l -> {
            try {
                l.onPlayerReadyChanged(roomCode, playerId, ready, true);
            } catch (RemoteException ignored) {}
        });

        return true;
    }

    @Override
    public boolean startGame(String roomCode, String hostPlayerId) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) {
            throw new RemoteException("Room " + roomCode + " not found.");
        }
        if (!room.hostId.equals(hostPlayerId)) {
            throw new RemoteException("Access denied: Only host can start match.");
        }
        if (room.players.size() < 2) {
            throw new RemoteException("Cannot start match: At least 2 players are required to start the battle (currently " + room.players.size() + ").");
        }
        for (PlayerDTO p : room.players.values()) {
            if (!p.isReady()) {
                throw new RemoteException("Cannot start match: Fighter '" + p.getNickname() + "' is not ready yet.");
            }
        }

        // Authoritatively launch the distributed match state machine
        room.startMatchSequence();
        return true;
    }

    @Override
    public GameStateDTO getRoomState(String roomCode, String playerId) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) return null;
        return room.toGameStateDTO(playerId);
    }

    @Override
    public QuestionDTO getCurrentQuestion(String roomCode) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) return null;
        return room.getCurrentQuestionDTO();
    }

    @Override
    public QuestionResultDTO getQuestionResult(String roomCode, int questionIndex) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) return null;
        return room.completedQuestionResults.get(questionIndex);
    }

    @Override
    public AnswerResultDTO submitAnswer(String roomCode, String playerId, int questionIndex,
                                        String selectedOption, long responseTimeMs) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) {
            return AnswerResultDTO.rejected("Room not found.");
        }
        return room.submitAnswer(playerId, questionIndex, selectedOption, responseTimeMs);
    }

    @Override
    public List<PlayerScoreDTO> getLeaderboard(String roomCode) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) return Collections.emptyList();
        return room.getLeaderboard();
    }

    @Override
    public QuestionDTO nextQuestion(String roomCode, String hostPlayerId) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) {
            throw new RemoteException("Room not found: " + roomCode);
        }
        return room.getCurrentQuestionDTO();
    }

    @Override
    public GameSummaryDTO endGame(String roomCode, String hostPlayerId) throws RemoteException {
        ActiveGameRoom room = getActiveRoom(roomCode);
        if (room == null) {
            throw new RemoteException("Room not found: " + roomCode);
        }
        return room.concludeMatch();
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    private ActiveGameRoom getActiveRoom(String roomCode) {
        if (roomCode == null) return null;
        return rooms.get(roomCode.trim().toUpperCase());
    }

    private String generateUniqueRoomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
            }
            code = sb.toString();
        } while (rooms.containsKey(code));
        return code;
    }

    // =========================================================================
    // ActiveGameRoom: Central Authoritative Distributed State Machine
    // WAITING -> COUNTDOWN -> QUESTION_ACTIVE -> QUESTION_RESULT -> NEXT_QUESTION -> FINISHED
    // =========================================================================
    private class ActiveGameRoom {
        final String roomCode;
        String hostId;
        final GameConfigDTO config;
        RoomState state = RoomState.WAITING;
        final Map<String, PlayerDTO> players = new LinkedHashMap<>();
        final Map<String, String> tokens = new ConcurrentHashMap<>();
        final List<FullQuestion> questions;
        int currentQuestionIndex = 0;
        final Map<Integer, Map<String, String>> answersPerRound = new ConcurrentHashMap<>(); // round -> (playerId -> option)
        final Map<Integer, QuestionResultDTO> completedQuestionResults = new ConcurrentHashMap<>();

        static class PlayerAnswerRecord {
            String playerId;
            int questionId;
            int questionIndex;
            String selectedOption;
            boolean isCorrect;
            int responseTimeMs;
            int pointsAwarded;
        }
        final List<PlayerAnswerRecord> matchAnswerLog = Collections.synchronizedList(new ArrayList<>());

        volatile int secondsRemaining = 0;
        volatile long matchStartTimeMs = 0;
        ScheduledFuture<?> currentTimerTask = null;
        final AtomicBoolean roundAdvancing = new AtomicBoolean(false);

        ActiveGameRoom(String roomCode, String hostId, String hostNickname, String hostAvatar,
                       String hostToken, GameConfigDTO config, List<FullQuestion> questions) {
            this.roomCode = roomCode;
            this.hostId = hostId;
            this.config = config;
            this.questions = questions;
            PlayerDTO host = new PlayerDTO(hostId, hostNickname, true, hostAvatar);
            host.setReady(true);
            players.put(hostId, host);
            tokens.put(hostId, hostToken);
        }

        synchronized int addPlayer(PlayerDTO player, String token) {
            if (players.size() >= 10) {
                return 1; // Room full
            }
            for (PlayerDTO p : players.values()) {
                if (p.getNickname().equalsIgnoreCase(player.getNickname())) {
                    return 2; // Duplicate nickname
                }
            }
            players.put(player.getPlayerId(), player);
            tokens.put(player.getPlayerId(), token);
            return 0; // Success
        }

        synchronized boolean removePlayer(String playerId) {
            PlayerDTO p = players.remove(playerId);
            tokens.remove(playerId);
            if (p != null && hostId.equals(playerId) && !players.isEmpty()) {
                String newHostId = players.keySet().iterator().next();
                hostId = newHostId;
                PlayerDTO newHost = players.get(newHostId);
                if (newHost != null) {
                    newHost.setHost(true);
                    newHost.setReady(true);
                    System.out.printf("[RMI Server] Host %s departed Room %s. Promoted %s (%s) to new Host!%n",
                            playerId, roomCode, newHost.getNickname(), newHostId);
                }
            }
            return p != null;
        }

        synchronized void setPlayerConnected(String playerId, boolean connected) {
            PlayerDTO p = players.get(playerId);
            if (p != null) {
                p.setConnected(connected);
            }
        }

        synchronized void setPlayerReady(String playerId, boolean ready) {
            PlayerDTO p = players.get(playerId);
            if (p != null) {
                p.setReady(ready);
            }
        }

        boolean isEmpty() {
            return players.isEmpty();
        }

        int getConnectedCount() {
            int count = 0;
            for (PlayerDTO p : players.values()) {
                if (p.isConnected()) count++;
            }
            return Math.max(1, count);
        }

        boolean haveAllConnectedPlayersAnswered(int qIndex) {
            Map<String, String> roundAns = answersPerRound.get(qIndex);
            if (roundAns == null) return false;
            for (PlayerDTO p : players.values()) {
                if (p.isConnected() && !roundAns.containsKey(p.getPlayerId())) {
                    return false;
                }
            }
            return true;
        }

        private synchronized void cancelTimer() {
            if (currentTimerTask != null && !currentTimerTask.isDone()) {
                currentTimerTask.cancel(true);
                currentTimerTask = null;
            }
        }

        // --- State Machine Transition: Start Battle ---
        synchronized void startMatchSequence() {
            state = RoomState.COUNTDOWN;
            cancelTimer();
            matchStartTimeMs = System.currentTimeMillis();

            System.out.println("================================================================================");
            System.out.printf("[DISTRIBUTED MATCH ENGINE] Room %s | Battle Launched by Host!%n", roomCode);
            System.out.printf("[STATE TRANSITION] WAITING -> COUNTDOWN (3s) | Mode: %s | Active Players: %d%n",
                    config.getGameMode(), players.size());
            System.out.println("================================================================================");

            notifyListeners(l -> {
                try {
                    l.onGameStarting(roomCode, 3);
                } catch (RemoteException ignored) {}
            });

            // Transition to Question 1 in 3 seconds
            globalScheduler.schedule(() -> startQuestionRound(1), 3, TimeUnit.SECONDS);
        }

        // --- State Machine Transition: Start Question Round ---
        synchronized void startQuestionRound(int qIndex) {
            cancelTimer();
            roundAdvancing.set(false);
            currentQuestionIndex = qIndex;
            state = RoomState.QUESTION_ACTIVE;

            FullQuestion q = questions.get(qIndex - 1);
            secondsRemaining = q.getTimeLimitSec();
            QuestionDTO questionDTO = q.toQuestionDTO(qIndex, questions.size(), config.getCategoryName());

            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("[DISTRIBUTED MATCH ENGINE] Room: %s | State: QUESTION_ACTIVE | Question %d/%d%n",
                    roomCode, qIndex, questions.size());
            System.out.printf("[PROMPT] %s%n", q.getQuestionText());
            System.out.printf("[TIME LIMIT] %ds | Mode: %s | Options: A, B, C, D%n",
                    secondsRemaining, config.getGameMode());
            System.out.println("--------------------------------------------------------------------------------");

            notifyListeners(l -> {
                try {
                    l.onQuestionStarted(roomCode, questionDTO, qIndex, questions.size(), secondsRemaining);
                } catch (RemoteException ignored) {}
            });

            // Authoritative 1-second countdown loop
            currentTimerTask = globalScheduler.scheduleAtFixedRate(() -> {
                try {
                    handleTick();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, 1, 1, TimeUnit.SECONDS);
        }

        private void handleTick() {
            int currentLeft;
            synchronized (this) {
                if (state != RoomState.QUESTION_ACTIVE) return;
                secondsRemaining--;
                currentLeft = secondsRemaining;
            }

            // Broadcast periodic timer tick to all connected clients
            notifyListeners(l -> {
                try {
                    l.onTimerTick(roomCode, currentLeft);
                } catch (RemoteException ignored) {}
            });

            if (currentLeft <= 0) {
                System.out.printf("[DISTRIBUTED TIMER] Room: %s | Question %d timer expired!%n",
                        roomCode, currentQuestionIndex);
                cancelTimer();
                concludeRound("TIMEOUT");
            }
        }

        // --- Synchronized Answer Submission ---
        synchronized AnswerResultDTO submitAnswer(String playerId, int qIndex, String selectedOption, long responseTimeMs) {
            if (selectedOption == null || !selectedOption.trim().matches("(?i)[A-D]")) {
                return AnswerResultDTO.rejected("Invalid answer option. Must be A, B, C, or D.");
            }
            PlayerDTO p = players.get(playerId);
            if (p == null) {
                return AnswerResultDTO.rejected("Player not in this room.");
            }
            if (state != RoomState.QUESTION_ACTIVE) {
                return AnswerResultDTO.rejected("Question round has expired or is not active.");
            }
            if (qIndex != currentQuestionIndex || qIndex <= 0 || qIndex > questions.size()) {
                return AnswerResultDTO.rejected("Question index mismatch.");
            }

            Map<String, String> roundAns = answersPerRound.computeIfAbsent(qIndex, k -> new ConcurrentHashMap<>());
            if (roundAns.putIfAbsent(playerId, selectedOption.toUpperCase()) != null) {
                return AnswerResultDTO.rejected("Answer already submitted for this round.");
            }

            FullQuestion q = questions.get(qIndex - 1);
            boolean isCorrect = q.getCorrectOption().equalsIgnoreCase(selectedOption.trim());

            // Authoritative scoring calculation via TechBattleQuizEngine
            ScoringResult scoring = TechBattleQuizEngine.evaluateAnswer(
                    config.getGameMode(),
                    isCorrect,
                    responseTimeMs,
                    q.getTimeLimitSec(),
                    q.getCorrectOption(),
                    q.getExplanation()
            );

            if (scoring.isCorrect()) {
                p.setCurrentStreak(p.getCurrentStreak() + 1);
                p.setCorrectAnswers(p.getCorrectAnswers() + 1);
            } else {
                p.setCurrentStreak(0);
                p.setWrongAnswers(p.getWrongAnswers() + 1);
            }
            p.setCumulativeResponseTimeMs(p.getCumulativeResponseTimeMs() + responseTimeMs);
            p.setScore(p.getScore() + scoring.getTotalPoints());

            // Record answer for persistence in MySQL match_answers
            PlayerAnswerRecord rec = new PlayerAnswerRecord();
            rec.playerId = playerId;
            rec.questionId = q.getId();
            rec.questionIndex = qIndex;
            rec.selectedOption = selectedOption.toUpperCase();
            rec.isCorrect = scoring.isCorrect();
            rec.responseTimeMs = (int) responseTimeMs;
            rec.pointsAwarded = scoring.getTotalPoints();
            matchAnswerLog.add(rec);

            System.out.printf("[DISTRIBUTED RPC] Room: %s | Fighter '%s' answered [%s] in %dms -> %s! (+%d pts, Streak: %d, Total: %d)%n",
                    roomCode, p.getNickname(), selectedOption, responseTimeMs,
                    scoring.isCorrect() ? "CORRECT" : "WRONG",
                    scoring.getTotalPoints(), p.getCurrentStreak(), p.getScore());

            // Notify listeners of answer submission and score change
            final int totalAns = roundAns.size();
            final int connectedCount = getConnectedCount();
            notifyListeners(l -> {
                try {
                    l.onAnswerSubmitted(roomCode, playerId, p.getNickname(), qIndex, totalAns, connectedCount);
                    l.onScoreUpdated(roomCode, playerId, p.getScore(), p.getCurrentStreak(), scoring.getTotalPoints());
                } catch (RemoteException ignored) {}
            });

            // If all connected players have answered, close the question early!
            if (haveAllConnectedPlayersAnswered(qIndex)) {
                System.out.printf("[DISTRIBUTED MATCH ENGINE] Room: %s | All %d active players answered Question %d early! Closing round immediately.%n",
                        roomCode, connectedCount, qIndex);
                cancelTimer();
                globalScheduler.execute(() -> concludeRound("ALL_ANSWERED"));
            }

            AnswerResultDTO evalResult = AnswerResultDTO.evaluated(
                    scoring.isCorrect(),
                    scoring.getTotalPoints(),
                    scoring.getBasePoints(),
                    scoring.getSpeedBonus(),
                    p.getScore(),
                    p.getCurrentStreak(),
                    responseTimeMs,
                    scoring.getCorrectOption(),
                    scoring.getExplanation()
            );
            evalResult.setPlayerId(playerId);
            return evalResult;
        }

        // --- State Machine Transition: Conclude Question Round ---
        void concludeRound(String reason) {
            if (!roundAdvancing.compareAndSet(false, true)) {
                return; // Guard against concurrent double-progression
            }
            cancelTimer();

            QuestionResultDTO result = new QuestionResultDTO();
            List<PlayerScoreDTO> standings;
            synchronized (this) {
                state = RoomState.QUESTION_RESULT;
                FullQuestion q = questions.get(currentQuestionIndex - 1);

                // For any connected player who timed out without answering, record wrong answer and timeLimit
                Map<String, String> roundAns = answersPerRound.get(currentQuestionIndex);
                long qTimeLimitMs = q.getTimeLimitSec() * 1000L;
                for (PlayerDTO p : players.values()) {
                    if (p.isConnected() && (roundAns == null || !roundAns.containsKey(p.getPlayerId()))) {
                        p.setCurrentStreak(0);
                        p.setWrongAnswers(p.getWrongAnswers() + 1);
                        p.setCumulativeResponseTimeMs(p.getCumulativeResponseTimeMs() + qTimeLimitMs);
                    }
                }

                Map<String, Integer> dist = new HashMap<>();
                dist.put("A", 0); dist.put("B", 0); dist.put("C", 0); dist.put("D", 0);
                if (roundAns != null) {
                    for (String opt : roundAns.values()) {
                        dist.put(opt, dist.getOrDefault(opt, 0) + 1);
                    }
                }

                standings = getLeaderboard();
                result.setQuestionIndex(currentQuestionIndex);
                result.setCorrectOption(q.getCorrectOption());
                result.setExplanation(q.getExplanation());
                result.setOptionDistribution(dist);
                result.setUpdatedLeaderboard(standings);
                result.setNextQuestionInSec(4);

                completedQuestionResults.put(currentQuestionIndex, result);
            }

            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("[STATE TRANSITION] QUESTION_ACTIVE -> QUESTION_RESULT (reason: %s)%n", reason);
            System.out.printf("[ROUND %d RESULT] Correct Option: %s%n", result.getQuestionIndex(), result.getCorrectOption());
            System.out.printf("[EXPLANATION] %s%n", result.getExplanation());
            if (!standings.isEmpty()) {
                System.out.printf("[LEADER] %s (%d PTS)%n", standings.get(0).getNickname(), standings.get(0).getScore());
            }
            System.out.println("[COUNTDOWN] Displaying result for 4 seconds...");
            System.out.println("--------------------------------------------------------------------------------");

            notifyListeners(l -> {
                try {
                    l.onQuestionEnded(roomCode, result);
                    l.onLeaderboardUpdated(roomCode, standings);
                } catch (RemoteException ignored) {}
            });

            // Update baseline previousRank for all players for the next round
            for (PlayerScoreDTO ps : standings) {
                PlayerDTO p = players.get(ps.getPlayerId());
                if (p != null) {
                    p.setPreviousRank(ps.getRank());
                }
            }

            // After 4s display, transition to NEXT_QUESTION or FINISHED
            globalScheduler.schedule(() -> {
                try {
                    if (currentQuestionIndex < questions.size()) {
                        synchronized (this) {
                            state = RoomState.NEXT_QUESTION;
                        }
                        System.out.printf("[STATE TRANSITION] QUESTION_RESULT -> NEXT_QUESTION | Preparing Question %d/%d...%n",
                                currentQuestionIndex + 1, questions.size());
                        startQuestionRound(currentQuestionIndex + 1);
                    } else {
                        finishMatch();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, 4, TimeUnit.SECONDS);
        }

        // --- State Machine Transition: Finish Match ---
        synchronized void finishMatch() {
            cancelTimer();
            state = RoomState.FINISHED;
            GameSummaryDTO summary = concludeMatch();

            System.out.println("================================================================================");
            System.out.println("[STATE TRANSITION] NEXT_QUESTION -> FINISHED");
            System.out.printf("[MATCH COMPLETED] Room: %s | Winner: '%s' with %d PTS!%n",
                    roomCode, summary.getWinnerNickname(), summary.getWinnerScore());
            if (summary.getFinalPodium() != null && !summary.getFinalPodium().isEmpty()) {
                System.out.println("[FINAL PODIUM]");
                for (int i = 0; i < summary.getFinalPodium().size(); i++) {
                    PlayerScoreDTO p = summary.getFinalPodium().get(i);
                    System.out.printf("   #%d: %s - %d PTS (Streak: %d)%n", i + 1, p.getNickname(), p.getScore(), p.getStreak());
                }
            }
            System.out.println("================================================================================");

            notifyListeners(l -> {
                try {
                    l.onGameFinished(roomCode, summary);
                } catch (RemoteException ignored) {}
            });
        }

        RoomDTO toRoomDTO() {
            RoomDTO dto = new RoomDTO(roomCode, hostId,
                    players.containsKey(hostId) ? players.get(hostId).getNickname() : "Host", config);
            dto.setState(state);
            dto.setPlayers(new ArrayList<>(players.values()));
            return dto;
        }

        GameStateDTO toGameStateDTO(String playerId) {
            GameStateDTO stateDTO = new GameStateDTO();
            stateDTO.setRoomCode(roomCode);
            stateDTO.setState(state);
            stateDTO.setConfig(config);
            stateDTO.setPlayers(new ArrayList<>(players.values()));
            stateDTO.setCurrentQuestionIndex(currentQuestionIndex);
            stateDTO.setTimeRemainingSec(secondsRemaining);
            stateDTO.setCurrentQuestion(getCurrentQuestionDTO());
            stateDTO.setCurrentLeaderboard(getLeaderboard());

            if (playerId != null && currentQuestionIndex > 0) {
                Map<String, String> roundAns = answersPerRound.get(currentQuestionIndex);
                if (roundAns != null && roundAns.containsKey(playerId)) {
                    stateDTO.setPlayerAlreadyAnswered(true);
                    stateDTO.setPlayerSelectedOption(roundAns.get(playerId));
                }
            }
            return stateDTO;
        }

        QuestionDTO getCurrentQuestionDTO() {
            if (currentQuestionIndex <= 0 || currentQuestionIndex > questions.size()) {
                return null;
            }
            FullQuestion q = questions.get(currentQuestionIndex - 1);
            return q.toQuestionDTO(currentQuestionIndex, questions.size(), config.getCategoryName());
        }

        List<PlayerScoreDTO> getLeaderboard() {
            List<PlayerScoreDTO> list = new ArrayList<>();
            for (PlayerDTO p : players.values()) {
                PlayerScoreDTO s = new PlayerScoreDTO(p.getPlayerId(), p.getNickname(), p.getScore(), 1, p.getCurrentStreak());
                s.setAvatar(p.getAvatar());
                s.setCorrectAnswers(p.getCorrectAnswers());
                s.setWrongAnswers(p.getWrongAnswers());
                int totalAnswered = p.getCorrectAnswers() + p.getWrongAnswers();
                s.setTotalAnswered(totalAnswered);
                int accuracy = totalAnswered > 0 ? (int) Math.round((double) p.getCorrectAnswers() * 100.0 / totalAnswered) : 0;
                s.setAccuracyPercentage(accuracy);
                s.setCumulativeAnswerTimeMs(p.getCumulativeResponseTimeMs());
                long avgResp = totalAnswered > 0 ? p.getCumulativeResponseTimeMs() / totalAnswered : 0;
                s.setAvgResponseTimeMs(avgResp);

                if (currentQuestionIndex > 0) {
                    Map<String, String> roundAns = answersPerRound.get(currentQuestionIndex);
                    s.setAnsweredCurrent(roundAns != null && roundAns.containsKey(p.getPlayerId()));
                }

                s.setPreviousRank(p.getPreviousRank());
                list.add(s);
            }

            // Authoritative Distributed Tie-Breaking:
            // 1. Total points (descending - higher score comes first)
            // 2. Total correct answers (descending)
            // 3. Cumulative answer time (ascending - faster is better)
            list.sort((a, b) -> {
                if (b.getScore() != a.getScore()) {
                    return Integer.compare(b.getScore(), a.getScore());
                }
                if (b.getCorrectAnswers() != a.getCorrectAnswers()) {
                    return Integer.compare(b.getCorrectAnswers(), a.getCorrectAnswers());
                }
                return Long.compare(a.getCumulativeAnswerTimeMs(), b.getCumulativeAnswerTimeMs());
            });

            for (int i = 0; i < list.size(); i++) {
                PlayerScoreDTO s = list.get(i);
                int newRank = i + 1;
                s.setRank(newRank);
                int prev = s.getPreviousRank();
                if (prev > 0) {
                    s.setRankChange(prev - newRank); // positive = moved up, negative = moved down, 0 = same
                } else {
                    s.setRankChange(0);
                }
                PlayerDTO p = players.get(s.getPlayerId());
                if (p != null) {
                    p.setRank(newRank);
                }
            }
            return list;
        }

        synchronized GameSummaryDTO concludeMatch() {
            state = RoomState.FINISHED;
            List<PlayerScoreDTO> standings = getLeaderboard();
            PlayerScoreDTO winner = standings.isEmpty() ? null : standings.get(0);

            GameSummaryDTO summary = new GameSummaryDTO();
            summary.setMatchUuid(UUID.randomUUID().toString());
            summary.setRoomCode(roomCode);
            summary.setWinnerNickname(winner != null ? winner.getNickname() : "Nobody");
            summary.setWinnerScore(winner != null ? winner.getScore() : 0);
            summary.setFullRankings(new ArrayList<>(standings));
            summary.setFinalPodium(new ArrayList<>(standings.subList(0, Math.min(3, standings.size()))));
            summary.setTotalQuestions(questions.size());
            summary.setGameMode(config.getGameMode().getDisplayName());
            summary.setDifficulty(config.getDifficulty().name());
            summary.setCategoryName(config.getCategoryName());
            long duration = matchStartTimeMs > 0 ? (System.currentTimeMillis() - matchStartTimeMs) / 1000 : 0;
            summary.setTotalDurationSec(duration);

            // Persist to MySQL if available
            persistMatchRecord(summary);

            return summary;
        }

        private void persistMatchRecord(GameSummaryDTO summary) {
            if (!DatabaseConfig.isAvailable()) return;
            String insertSql = "INSERT INTO game_matches (match_uuid, room_code, game_mode, difficulty, category_id, host_nickname, total_players, winner_nickname, winner_score, started_at, ended_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, summary.getMatchUuid());
                stmt.setString(2, summary.getRoomCode());
                stmt.setString(3, config.getGameMode().name());
                stmt.setString(4, config.getDifficulty().name());
                int catId = (config != null && config.getCategoryId() >= 1 && config.getCategoryId() <= 6)
                        ? config.getCategoryId() : 1;
                stmt.setInt(5, catId);
                stmt.setString(6, players.containsKey(hostId) ? players.get(hostId).getNickname() : "Host");
                stmt.setInt(7, players.size());
                stmt.setString(8, summary.getWinnerNickname());
                stmt.setInt(9, summary.getWinnerScore());
                stmt.executeUpdate();

                long matchId = -1;
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        matchId = rs.getLong(1);
                    }
                }

                if (matchId > 0 && summary.getFullRankings() != null) {
                    String insertPlayerSql = "INSERT INTO match_players (match_id, player_id, nickname, final_score, correct_answers, wrong_answers, avg_response_time_ms, rank_position) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement pStmt = conn.prepareStatement(insertPlayerSql)) {
                        for (PlayerScoreDTO ps : summary.getFullRankings()) {
                            pStmt.setLong(1, matchId);
                            pStmt.setString(2, ps.getPlayerId());
                            pStmt.setString(3, ps.getNickname());
                            pStmt.setInt(4, ps.getScore());
                            pStmt.setInt(5, ps.getCorrectAnswers());
                            pStmt.setInt(6, ps.getWrongAnswers());
                            pStmt.setInt(7, (int) ps.getAvgResponseTimeMs());
                            pStmt.setInt(8, ps.getRank());
                            pStmt.addBatch();
                        }
                        pStmt.executeBatch();
                    }
                }

                if (matchId > 0 && !matchAnswerLog.isEmpty()) {
                    String insertAnsSql = "INSERT INTO match_answers (match_id, player_id, question_id, question_index, selected_option, is_correct, response_time_ms, points_awarded, answered_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
                    try (PreparedStatement aStmt = conn.prepareStatement(insertAnsSql)) {
                        for (PlayerAnswerRecord rec : matchAnswerLog) {
                            if (rec.questionId <= 0) continue;
                            aStmt.setLong(1, matchId);
                            aStmt.setString(2, rec.playerId);
                            aStmt.setInt(3, rec.questionId);
                            aStmt.setInt(4, rec.questionIndex);
                            aStmt.setString(5, rec.selectedOption);
                            aStmt.setBoolean(6, rec.isCorrect);
                            aStmt.setInt(7, rec.responseTimeMs);
                            aStmt.setInt(8, rec.pointsAwarded);
                            aStmt.addBatch();
                        }
                        aStmt.executeBatch();
                    } catch (Exception ex) {
                        System.err.println("[QuizRemoteImpl] Warning saving match answers: " + ex.getMessage());
                    }
                }
                System.out.println("[QuizRemoteImpl] Match summary, players, and answers persisted to MySQL.");
            } catch (Exception e) {
                System.err.println("[QuizRemoteImpl] Could not archive match to MySQL: " + e.getMessage());
            }
        }

        void cleanup() {
            cancelTimer();
        }
    }
}
