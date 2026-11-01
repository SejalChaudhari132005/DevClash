package com.devclash.gateway.callback;

import com.devclash.common.dto.*;
import com.devclash.common.rmi.GameCallbackListener;
import com.devclash.common.rmi.QuizRemote;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RMI Callback Receiver in Spring Boot Gateway.
 * Receives authoritative asynchronous remote events from the Java RMI Quiz Server and
 * translates them into WebSocket STOMP topic broadcasts:
 *  - GAME_STARTED
 *  - QUESTION_STARTED
 *  - TIMER_UPDATE
 *  - ANSWER_SUBMITTED
 *  - QUESTION_ENDED
 *  - SCORE_UPDATED
 *  - LEADERBOARD_UPDATED
 *  - GAME_FINISHED
 */
@Component
public class GatewayCallbackHandler implements GameCallbackListener {

    private static final Logger log = LoggerFactory.getLogger(GatewayCallbackHandler.class);

    private final QuizRemote quizRemote;
    private final SimpMessagingTemplate messagingTemplate;
    private GameCallbackListener exportedStub;
    private volatile boolean registered = false;
    private final String listenerId = "gateway-" + UUID.randomUUID().toString().substring(0, 8);

    @org.springframework.beans.factory.annotation.Value("${devclash.rmi.callback-port:${RMI_CALLBACK_PORT:0}}")
    private int callbackPort;

    @org.springframework.beans.factory.annotation.Value("${devclash.rmi.callback-host:${RMI_CALLBACK_HOST:}}")
    private String callbackHost;

    public GatewayCallbackHandler(QuizRemote quizRemote, SimpMessagingTemplate messagingTemplate) {
        this.quizRemote = quizRemote;
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            if (callbackHost != null && !callbackHost.isBlank()) {
                System.setProperty("java.rmi.server.hostname", callbackHost.trim());
                log.info("[Gateway Callback] Configured java.rmi.server.hostname for callback: {}", callbackHost.trim());
            }
            // Export this instance as a remote callback object on callbackPort (0 = dynamic, or fixed port e.g. 1097)
            exportedStub = (GameCallbackListener) UnicastRemoteObject.exportObject(this, callbackPort);
            log.info("[Gateway Callback] Successfully exported RMI GameCallbackListener stub on port {}.",
                    callbackPort == 0 ? "dynamic" : String.valueOf(callbackPort));
            startRegistrationWorker();
        } catch (RemoteException e) {
            log.error("[Gateway Callback] Failed to export callback stub: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (quizRemote != null && registered) {
            try {
                quizRemote.unregisterCallbackListener(listenerId);
                log.info("[Gateway Callback] Unregistered listener '{}' from QuizRemote Server.", listenerId);
            } catch (Exception ignored) {}
        }
        if (exportedStub != null) {
            try {
                UnicastRemoteObject.unexportObject(this, true);
                log.info("[Gateway Callback] Unexported RMI GameCallbackListener stub.");
            } catch (NoSuchObjectException ignored) {}
        }
    }

    public GameCallbackListener getExportedStub() {
        return exportedStub != null ? exportedStub : this;
    }

    /**
     * Resilient background registration loop that attempts to register with the RMI server.
     */
    public void startRegistrationWorker() {
        Thread thread = new Thread(() -> {
            int retries = 0;
            while (!registered && retries < 60) {
                try {
                    if (exportedStub != null) {
                        quizRemote.registerCallbackListener(listenerId, exportedStub);
                        registered = true;
                        log.info("[Gateway Callback] Successfully registered RMI GameCallbackListener ('{}') with QuizRemote Server!", listenerId);
                        break;
                    }
                } catch (Exception e) {
                    retries++;
                    log.debug("[Gateway Callback] Waiting for RMI Server to register callback listener (retry {}): {}", retries, e.getMessage());
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "rmi-callback-registrar");
        thread.setDaemon(true);
        thread.start();
    }

    public void ensureRegistered() {
        if (!registered && exportedStub != null) {
            try {
                quizRemote.registerCallbackListener(listenerId, exportedStub);
                registered = true;
                log.info("[Gateway Callback] On-demand registration of RMI GameCallbackListener succeeded.");
            } catch (Exception e) {
                log.warn("[Gateway Callback] On-demand registration attempt failed: {}", e.getMessage());
            }
        }
    }

    @Override
    public void onPlayerJoined(String roomCode, PlayerDTO player) throws RemoteException {
        log.info("[Gateway Callback] onPlayerJoined for Room: {} - Player: {}", roomCode, player.getNickname());
        try {
            RoomDTO room = quizRemote.getRoom(roomCode);
            Map<String, Object> payload = new HashMap<>();
            payload.put("player", player);
            payload.put("playerId", player.getPlayerId());
            payload.put("nickname", player.getNickname());
            payload.put("avatar", player.getAvatar());
            payload.put("host", player.isHost());
            payload.put("isHost", player.isHost());
            payload.put("ready", player.isReady());
            payload.put("connected", player.isConnected());
            if (room != null) {
                payload.put("room", room);
            }
            broadcast(roomCode, "PLAYER_JOINED", payload);
            if (room != null) {
                broadcast(roomCode, "ROOM_UPDATED", Map.of("room", room));
            }
        } catch (Exception e) {
            log.warn("[Gateway Callback] Error querying room on player join: {}", e.getMessage());
            Map<String, Object> payload = new HashMap<>();
            payload.put("player", player);
            payload.put("playerId", player.getPlayerId());
            payload.put("nickname", player.getNickname());
            broadcast(roomCode, "PLAYER_JOINED", payload);
        }
    }

    @Override
    public void onPlayerLeft(String roomCode, String playerId, String reason) throws RemoteException {
        log.info("[Gateway Callback] onPlayerLeft for Room: {} - Player: {} ({})", roomCode, playerId, reason);
        try {
            RoomDTO room = quizRemote.getRoom(roomCode);
            Map<String, Object> payload = new HashMap<>();
            payload.put("playerId", playerId);
            payload.put("reason", reason);
            if (room != null) {
                payload.put("room", room);
            }
            broadcast(roomCode, "PLAYER_LEFT", payload);
            if (room != null) {
                broadcast(roomCode, "ROOM_UPDATED", Map.of("room", room));
            }
        } catch (Exception e) {
            log.warn("[Gateway Callback] Error querying room on player left: {}", e.getMessage());
            Map<String, String> payload = new HashMap<>();
            payload.put("playerId", playerId);
            payload.put("reason", reason);
            broadcast(roomCode, "PLAYER_LEFT", payload);
        }
    }

    @Override
    public void onPlayerReadyChanged(String roomCode, String playerId, boolean isReady, boolean isConnected) throws RemoteException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("playerId", playerId);
        payload.put("ready", isReady);
        payload.put("connected", isConnected);
        try {
            RoomDTO room = quizRemote.getRoom(roomCode);
            if (room != null) {
                payload.put("room", room);
            }
            broadcast(roomCode, "PLAYER_READY_CHANGED", payload);
            broadcast(roomCode, "PLAYER_READY", payload);
            if (room != null) {
                broadcast(roomCode, "ROOM_UPDATED", Map.of("room", room));
            }
        } catch (Exception e) {
            broadcast(roomCode, "PLAYER_READY_CHANGED", payload);
            broadcast(roomCode, "PLAYER_READY", payload);
        }
    }

    @Override
    public void onGameStarting(String roomCode, int countdownSeconds) throws RemoteException {
        log.info("[Gateway Callback] onGameStarting for Room: {} - Countdown: {}s", roomCode, countdownSeconds);
        Map<String, Object> payload = new HashMap<>();
        payload.put("roomCode", roomCode);
        payload.put("countdown", countdownSeconds);
        payload.put("state", "COUNTDOWN");
        payload.put("message", "Match starting in " + countdownSeconds + " seconds...");

        // Broadcast GAME_STARTED and GAME_STARTING
        broadcast(roomCode, "GAME_STARTED", payload);
        broadcast(roomCode, "GAME_STARTING", payload);
    }

    @Override
    public void onQuestionStarted(String roomCode, QuestionDTO question, int questionIndex,
                                  int totalQuestions, int timeLimitSec) throws RemoteException {
        log.info("[Gateway Callback] onQuestionStarted for Room: {} - Question {}/{} ({}s)",
                roomCode, questionIndex, totalQuestions, timeLimitSec);
        Map<String, Object> payload = new HashMap<>();
        payload.put("roomCode", roomCode);
        payload.put("question", question);
        payload.put("questionIndex", questionIndex);
        payload.put("totalQuestions", totalQuestions);
        payload.put("timeLimitSec", timeLimitSec);

        broadcast(roomCode, "QUESTION_STARTED", payload);
    }

    @Override
    public void onTimerTick(String roomCode, int secondsRemaining) throws RemoteException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("roomCode", roomCode);
        payload.put("secondsRemaining", secondsRemaining);
        payload.put("timeRemaining", secondsRemaining);

        // Broadcast TIMER_UPDATE and TIMER_TICK for full compatibility
        broadcast(roomCode, "TIMER_UPDATE", payload);
        broadcast(roomCode, "TIMER_TICK", payload);
    }

    @Override
    public void onAnswerSubmitted(String roomCode, String playerId, String nickname, int questionIndex,
                                  int totalAnswered, int totalConnected) throws RemoteException {
        log.info("[Gateway Callback] onAnswerSubmitted: {} ({}) answered Q{} ({}/{} answered)",
                nickname, playerId, questionIndex, totalAnswered, totalConnected);
        Map<String, Object> payload = new HashMap<>();
        payload.put("roomCode", roomCode);
        payload.put("playerId", playerId);
        payload.put("nickname", nickname);
        payload.put("questionIndex", questionIndex);
        payload.put("totalAnswered", totalAnswered);
        payload.put("totalConnected", totalConnected);

        broadcast(roomCode, "ANSWER_SUBMITTED", payload);
    }

    @Override
    public void onScoreUpdated(String roomCode, String playerId, int newScore, int streak, int pointsAdded) throws RemoteException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("roomCode", roomCode);
        payload.put("playerId", playerId);
        payload.put("newScore", newScore);
        payload.put("score", newScore);
        payload.put("streak", streak);
        payload.put("pointsAdded", pointsAdded);

        broadcast(roomCode, "SCORE_UPDATED", payload);
    }

    @Override
    public void onQuestionEnded(String roomCode, QuestionResultDTO result) throws RemoteException {
        log.info("[Gateway Callback] onQuestionEnded for Room: {} - Correct: {}",
                roomCode, result.getCorrectOption());
        broadcast(roomCode, "QUESTION_ENDED", result);
    }

    @Override
    public void onLeaderboardUpdated(String roomCode, List<PlayerScoreDTO> leaderboard) throws RemoteException {
        broadcast(roomCode, "LEADERBOARD_UPDATED", leaderboard);
    }

    @Override
    public void onGameFinished(String roomCode, GameSummaryDTO summary) throws RemoteException {
        log.info("[Gateway Callback] onGameFinished for Room: {} - Winner: {} ({} PTS)",
                roomCode, summary.getWinnerNickname(), summary.getWinnerScore());
        broadcast(roomCode, "GAME_FINISHED", summary);
    }

    @Override
    public void onRoomClosed(String roomCode, String reason) throws RemoteException {
        Map<String, String> payload = new HashMap<>();
        payload.put("roomCode", roomCode);
        payload.put("reason", reason);
        broadcast(roomCode, "ROOM_CLOSED", payload);
    }

    private void broadcast(String roomCode, String eventType, Object data) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("eventType", eventType);
        msg.put("roomCode", roomCode);
        msg.put("timestamp", System.currentTimeMillis());
        msg.put("payload", data);

        messagingTemplate.convertAndSend("/topic/room/" + roomCode, msg);
    }
}
