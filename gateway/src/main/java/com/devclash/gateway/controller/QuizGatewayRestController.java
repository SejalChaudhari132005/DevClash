package com.devclash.gateway.controller;

import com.devclash.common.dto.*;
import com.devclash.common.enums.Difficulty;
import com.devclash.common.enums.GameMode;
import com.devclash.common.rmi.QuizRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Gateway Controller that invokes remote methods on the separate Java RMI Quiz Server.
 * Demonstrates pure RPC invocations over JRMP protocol.
 */
@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "*")
public class QuizGatewayRestController {

    private static final Logger log = LoggerFactory.getLogger(QuizGatewayRestController.class);
    private final QuizRemote quizRemote;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public QuizGatewayRestController(QuizRemote quizRemote, org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.quizRemote = quizRemote;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> pingRmi() {
        Map<String, Object> resp = new HashMap<>();
        try {
            boolean status = quizRemote.ping();
            resp.put("success", true);
            resp.put("rmiConnected", status);
            resp.put("message", "Successfully invoked ping() on remote RMI Quiz Server!");
            return ResponseEntity.ok(resp);
        } catch (RemoteException e) {
            log.error("RMI ping failed: {}", e.getMessage());
            resp.put("success", false);
            resp.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(resp);
        }
    }

    @PostMapping("/room/create")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, Object> req) {
        String nickname = (String) req.getOrDefault("nickname", "HostUser");
        String avatar = (String) req.getOrDefault("avatar", "terminal");
        try {
            GameConfigDTO config = null;
            if (req.containsKey("categoryId") || req.containsKey("gameMode") || req.containsKey("difficulty") || req.containsKey("config")) {
                Map<String, Object> cfgMap = req.containsKey("config") && req.get("config") instanceof Map
                        ? (Map<String, Object>) req.get("config")
                        : req;
                int categoryId = ((Number) cfgMap.getOrDefault("categoryId", 1)).intValue();
                String categoryName = (String) cfgMap.getOrDefault("categoryName", "Programming & Languages");
                String diffStr = (String) cfgMap.getOrDefault("difficulty", "MEDIUM");
                String modeStr = (String) cfgMap.getOrDefault("gameMode", "CLASSIC");
                Difficulty difficulty = Difficulty.valueOf(diffStr.toUpperCase());
                GameMode gameMode = GameMode.valueOf(modeStr.toUpperCase());
                config = new GameConfigDTO(categoryId, categoryName, difficulty, gameMode);
                if (cfgMap.containsKey("totalQuestions")) {
                    config.setTotalQuestions(((Number) cfgMap.get("totalQuestions")).intValue());
                }
                if (cfgMap.containsKey("timeLimitSec")) {
                    config.setQuestionTimeLimitSec(((Number) cfgMap.get("timeLimitSec")).intValue());
                }
            }
            log.info("[Gateway REST] Invoking remote createRoom('{}') via RMI stub...", nickname);
            RoomDTO room = quizRemote.createRoom(nickname, avatar, config);
            log.info("[Gateway REST] Remote RMI invocation succeeded! Room code: {}", room.getRoomCode());

            String topic = "/topic/room/" + room.getRoomCode();
            messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("ROOM_CREATED", Map.of(
                    "success", true,
                    "room", room,
                    "playerId", room.getHostPlayerId(),
                    "sessionToken", room.getHostSessionToken() != null ? room.getHostSessionToken() : ""
            )));
            messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("ROOM_UPDATED", Map.of("room", room)));

            return ResponseEntity.ok(room);
        } catch (RemoteException e) {
            log.error("Remote createRoom call failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "RMI Server communication error: " + e.getMessage()));
        }
    }

    @PostMapping("/room/join")
    public ResponseEntity<?> joinRoom(@RequestBody Map<String, String> req) {
        String roomCode = req.get("roomCode");
        String nickname = req.get("nickname");
        String avatar = req.getOrDefault("avatar", "robot");
        if (roomCode != null) roomCode = roomCode.trim().toUpperCase();

        try {
            log.info("[Gateway REST] Invoking remote joinRoom('{}', '{}') via RMI stub...", roomCode, nickname);
            JoinResultDTO result = quizRemote.joinRoom(roomCode, nickname, avatar);
            if (result.isSuccess() && result.getRoom() != null) {
                String topic = "/topic/room/" + roomCode;
                PlayerDTO joinedPlayer = null;
                for (PlayerDTO p : result.getRoom().getPlayers()) {
                    if (p.getPlayerId().equals(result.getPlayerId())) {
                        joinedPlayer = p;
                        break;
                    }
                }
                messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("PLAYER_JOINED", Map.of(
                        "player", joinedPlayer != null ? joinedPlayer : Map.of("playerId", result.getPlayerId(), "nickname", nickname),
                        "room", result.getRoom()
                )));
                messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("ROOM_UPDATED", Map.of("room", result.getRoom())));
            }
            return ResponseEntity.ok(result);
        } catch (RemoteException e) {
            log.error("Remote joinRoom call failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "RMI joinRoom error: " + e.getMessage()));
        }
    }

    @PostMapping("/room/ready")
    public ResponseEntity<?> setReady(@RequestBody Map<String, Object> req) {
        String roomCode = (String) req.get("roomCode");
        String playerId = (String) req.get("playerId");
        boolean ready = Boolean.TRUE.equals(req.get("ready"));
        if (roomCode != null) roomCode = roomCode.trim().toUpperCase();

        try {
            boolean ok = quizRemote.setPlayerReady(roomCode, playerId, ready);
            RoomDTO updatedRoom = quizRemote.getRoom(roomCode);
            if (updatedRoom != null) {
                String topic = "/topic/room/" + roomCode;
                messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("PLAYER_READY", Map.of(
                        "playerId", playerId,
                        "ready", ready,
                        "room", updatedRoom
                )));
                messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("ROOM_UPDATED", Map.of("room", updatedRoom)));
            }
            return ResponseEntity.ok(Map.of("success", ok));
        } catch (RemoteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/room/remove")
    public ResponseEntity<?> removePlayer(@RequestBody Map<String, String> req) {
        String roomCode = req.get("roomCode");
        String hostPlayerId = req.get("hostPlayerId");
        String targetPlayerId = req.get("targetPlayerId");
        if (roomCode != null) roomCode = roomCode.trim().toUpperCase();

        try {
            log.info("[Gateway REST] Host '{}' removing player '{}' from room '{}'", hostPlayerId, targetPlayerId, roomCode);
            boolean removed = quizRemote.removePlayer(roomCode, hostPlayerId, targetPlayerId);
            RoomDTO updatedRoom = quizRemote.getRoom(roomCode);
            String topic = "/topic/room/" + roomCode;

            messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("PLAYER_LEFT", Map.of(
                    "playerId", targetPlayerId,
                    "kicked", true,
                    "reason", "Removed by host",
                    "room", updatedRoom != null ? updatedRoom : Map.of()
            )));

            if (updatedRoom != null) {
                messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("ROOM_UPDATED", Map.of("room", updatedRoom)));
            }
            return ResponseEntity.ok(Map.of("success", removed));
        } catch (SecurityException e) {
            log.warn("removePlayer security exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        } catch (RemoteException e) {
            log.error("removePlayer error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/room/start")
    public ResponseEntity<?> startGame(@RequestBody Map<String, String> req) {
        String roomCode = req.get("roomCode");
        String hostPlayerId = req.get("hostPlayerId");
        if (roomCode != null) roomCode = roomCode.trim().toUpperCase();

        try {
            log.info("[Gateway REST] Invoking remote startGame('{}') via RMI stub...", roomCode);
            boolean started = quizRemote.startGame(roomCode, hostPlayerId);
            return ResponseEntity.ok(Map.of("success", started));
        } catch (SecurityException e) {
            log.warn("startGame security exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        } catch (RemoteException e) {
            log.error("startGame error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/room/{roomCode}")
    public ResponseEntity<?> getRoom(@PathVariable("roomCode") String roomCode) {
        String cleanCode = roomCode != null ? roomCode.trim().toUpperCase() : "";
        try {
            RoomDTO room = quizRemote.getRoom(cleanCode);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Room not found"));
            }
            return ResponseEntity.ok(room);
        } catch (RemoteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/room/{roomCode}/state")
    public ResponseEntity<?> getRoomState(@PathVariable("roomCode") String roomCode,
                                          @RequestParam(value = "playerId", required = false) String playerId) {
        String cleanCode = roomCode != null ? roomCode.trim().toUpperCase() : "";
        try {
            GameStateDTO state = quizRemote.getRoomState(cleanCode, playerId);
            if (state == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Room not found"));
            }
            RoomDTO room = quizRemote.getRoom(cleanCode);
            Map<String, Object> resp = new HashMap<>();
            resp.put("roomCode", cleanCode);
            resp.put("state", state);
            resp.put("room", room);
            resp.put("players", state.getPlayers());
            resp.put("currentQuestion", state.getCurrentQuestion());
            resp.put("currentQuestionIndex", state.getCurrentQuestionIndex());
            resp.put("timeRemainingSec", state.getTimeRemainingSec());
            resp.put("currentLeaderboard", state.getCurrentLeaderboard());
            if (room != null) {
                resp.put("hostPlayerId", room.getHostPlayerId());
                resp.put("hostNickname", room.getHostNickname());
                resp.put("config", room.getConfig());
            }

            // If match is finished, automatically attach the final verified GameSummaryDTO
            if (state.getState() == com.devclash.common.enums.RoomState.FINISHED ||
                    (room != null && room.getState() == com.devclash.common.enums.RoomState.FINISHED)) {
                try {
                    GameSummaryDTO summary = quizRemote.endGame(cleanCode, "SYSTEM");
                    if (summary != null) {
                        resp.put("gameSummary", summary);
                    }
                } catch (Exception ignored) {}
            }

            return ResponseEntity.ok(resp);
        } catch (RemoteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/room/{roomCode}/summary")
    public ResponseEntity<?> getMatchSummary(@PathVariable("roomCode") String roomCode) {
        String cleanCode = roomCode != null ? roomCode.trim().toUpperCase() : "";
        try {
            GameSummaryDTO summary = quizRemote.endGame(cleanCode, "SYSTEM");
            return ResponseEntity.ok(summary != null ? summary : Map.of("error", "No summary available"));
        } catch (RemoteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/room/{roomCode}/question")
    public ResponseEntity<?> getCurrentQuestion(@PathVariable("roomCode") String roomCode) {
        try {
            QuestionDTO question = quizRemote.getCurrentQuestion(roomCode);
            return ResponseEntity.ok(question);
        } catch (RemoteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/room/answer")
    public ResponseEntity<?> submitAnswer(@RequestBody Map<String, Object> req) {
        String roomCode = (String) req.get("roomCode");
        String playerId = (String) req.get("playerId");
        int questionIndex = ((Number) req.getOrDefault("questionIndex", 1)).intValue();
        String selectedOption = (String) req.get("selectedOption");
        long responseTimeMs = ((Number) req.getOrDefault("responseTimeMs", 2500)).longValue();
        if (roomCode != null) roomCode = roomCode.trim().toUpperCase();
        try {
            log.info("[Gateway REST] Invoking remote submitAnswer('{}', '{}', opt='{}') via RMI stub...",
                    roomCode, playerId, selectedOption);
            AnswerResultDTO result = quizRemote.submitAnswer(roomCode, playerId, questionIndex, selectedOption, responseTimeMs);
            if (result != null) {
                result.setPlayerId(playerId);
                messagingTemplate.convertAndSend("/topic/room/" + roomCode,
                        WebSocketGameController.wrapEvent("ANSWER_ACK", result));
            }
            return ResponseEntity.ok(result);
        } catch (RemoteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/room/{roomCode}/leaderboard")
    public ResponseEntity<?> getLeaderboard(@PathVariable("roomCode") String roomCode) {
        try {
            List<PlayerScoreDTO> leaderboard = quizRemote.getLeaderboard(roomCode);
            return ResponseEntity.ok(leaderboard);
        } catch (RemoteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/room/next")
    public ResponseEntity<?> nextQuestion(@RequestBody Map<String, String> req) {
        String roomCode = req.get("roomCode");
        String hostPlayerId = req.get("hostPlayerId");
        try {
            QuestionDTO next = quizRemote.nextQuestion(roomCode, hostPlayerId);
            return ResponseEntity.ok(next != null ? next : Map.of("matchEnded", true));
        } catch (RemoteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/room/end")
    public ResponseEntity<?> endGame(@RequestBody Map<String, String> req) {
        String roomCode = req.get("roomCode");
        String hostPlayerId = req.get("hostPlayerId");
        try {
            GameSummaryDTO summary = quizRemote.endGame(roomCode, hostPlayerId);
            return ResponseEntity.ok(summary);
        } catch (RemoteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/room/leave")
    public ResponseEntity<?> leaveRoom(@RequestBody Map<String, String> req) {
        String roomCode = req.get("roomCode");
        String playerId = req.get("playerId");
        if (roomCode != null) roomCode = roomCode.trim().toUpperCase();
        try {
            log.info("[Gateway REST] Player '{}' leaving room '{}'", playerId, roomCode);
            boolean ok = quizRemote.leaveRoom(roomCode, playerId);
            RoomDTO updatedRoom = quizRemote.getRoom(roomCode);
            String topic = "/topic/room/" + roomCode;

            messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("PLAYER_LEFT", Map.of(
                    "playerId", playerId != null ? playerId : "",
                    "reason", "Player left the room",
                    "room", updatedRoom != null ? updatedRoom : Map.of()
            )));

            if (updatedRoom != null) {
                messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("ROOM_UPDATED", Map.of("room", updatedRoom)));
            }
            return ResponseEntity.ok(Map.of("success", ok));
        } catch (RemoteException e) {
            log.error("[Gateway REST] leaveRoom error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/room/reconnect")
    public ResponseEntity<?> reconnectPlayer(@RequestBody Map<String, String> req) {
        String roomCode = req.get("roomCode");
        String playerId = req.get("playerId");
        if (roomCode != null) roomCode = roomCode.trim().toUpperCase();
        try {
            boolean ok = quizRemote.setPlayerConnected(roomCode, playerId, true);
            RoomDTO updatedRoom = quizRemote.getRoom(roomCode);
            if (ok && updatedRoom != null) {
                String topic = "/topic/room/" + roomCode;
                messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("PLAYER_RECONNECTED", Map.of(
                        "playerId", playerId,
                        "connected", true,
                        "room", updatedRoom
                )));
                messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("ROOM_UPDATED", Map.of("room", updatedRoom)));
            }
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", ok);
            if (updatedRoom != null) {
                resp.put("room", updatedRoom);
            }
            return ResponseEntity.ok(resp);
        } catch (RemoteException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
