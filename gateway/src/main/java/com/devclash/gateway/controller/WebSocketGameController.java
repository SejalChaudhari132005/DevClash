package com.devclash.gateway.controller;

import com.devclash.common.dto.*;
import com.devclash.common.rmi.QuizRemote;
import com.devclash.gateway.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket STOMP Controller for DevClash.
 * Handles multiplayer room creation, joining, lobby ready-states, player kicks, and game start.
 * All state is maintained centrally by the Java RMI Server process.
 */
@Controller
public class WebSocketGameController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketGameController.class);

    private final QuizRemote quizRemote;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.devclash.gateway.callback.GatewayCallbackHandler callbackHandler;

    public WebSocketGameController(QuizRemote quizRemote, SimpMessagingTemplate messagingTemplate,
                                   com.devclash.gateway.callback.GatewayCallbackHandler callbackHandler) {
        this.quizRemote = quizRemote;
        this.messagingTemplate = messagingTemplate;
        this.callbackHandler = callbackHandler;
    }

    @MessageMapping("/room.create")
    public void createRoom(@Payload CreateRoomRequest req, org.springframework.messaging.simp.SimpMessageHeaderAccessor headerAccessor) {
        try {
            log.info("[WS Gateway] /room.create requested by '{}'", req.getNickname());
            RoomDTO room = quizRemote.createRoom(
                    req.getNickname(),
                    req.getAvatar(),
                    req.getConfig()
            );

            if (headerAccessor != null && headerAccessor.getSessionId() != null) {
                com.devclash.gateway.listener.WebSocketEventListener.registerSession(
                        headerAccessor.getSessionId(), room.getRoomCode(), room.getHostPlayerId());
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("room", room);
            resp.put("playerId", room.getHostPlayerId());
            resp.put("sessionToken", room.getHostSessionToken());

            String topic = "/topic/room/" + room.getRoomCode();
            messagingTemplate.convertAndSend(topic, wrapEvent("ROOM_CREATED", resp));
            messagingTemplate.convertAndSend(topic, wrapEvent("ROOM_UPDATED", Map.of("room", room)));
        } catch (RemoteException e) {
            log.error("[WS Gateway] createRoom error: {}", e.getMessage());
            sendError(null, "Failed to create room via RMI Server: " + e.getMessage());
        }
    }

    @MessageMapping("/room.join")
    public void joinRoom(@Payload JoinRoomRequest req, org.springframework.messaging.simp.SimpMessageHeaderAccessor headerAccessor) {
        String roomCode = req.getRoomCode() != null ? req.getRoomCode().trim().toUpperCase() : "";
        try {
            log.info("[WS Gateway] /room.join requested by '{}' for Room '{}'", req.getNickname(), roomCode);
            JoinResultDTO result = quizRemote.joinRoom(
                    roomCode,
                    req.getNickname(),
                    req.getAvatar()
            );

            if (!result.isSuccess()) {
                log.warn("[WS Gateway] Join rejected for '{}': {}", req.getNickname(), result.getMessage());
                sendError(roomCode, result.getMessage());
                return;
            }

            if (headerAccessor != null && headerAccessor.getSessionId() != null) {
                com.devclash.gateway.listener.WebSocketEventListener.registerSession(
                        headerAccessor.getSessionId(), roomCode, result.getPlayerId());
            }

            RoomDTO room = result.getRoom();
            PlayerDTO joinedPlayer = null;
            if (room != null && room.getPlayers() != null) {
                for (PlayerDTO p : room.getPlayers()) {
                    if (p.getPlayerId().equals(result.getPlayerId())) {
                        joinedPlayer = p;
                        break;
                    }
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("playerId", result.getPlayerId());
            resp.put("sessionToken", result.getSessionToken());
            resp.put("room", room);
            resp.put("player", joinedPlayer);

            String topic = "/topic/room/" + roomCode;
            // 1. Confirm to the joining player
            messagingTemplate.convertAndSend(topic, wrapEvent("JOIN_SUCCESS", resp));
            // 2. Broadcast PLAYER_JOINED to everyone in the room
            messagingTemplate.convertAndSend(topic, wrapEvent("PLAYER_JOINED", Map.of(
                    "player", joinedPlayer != null ? joinedPlayer : Map.of("playerId", result.getPlayerId(), "nickname", req.getNickname()),
                    "room", room
            )));
            // 3. Broadcast ROOM_UPDATED to ensure completely synchronized player list
            messagingTemplate.convertAndSend(topic, wrapEvent("ROOM_UPDATED", Map.of("room", room)));

            log.info("[WS Gateway] Player '{}' ({}) successfully joined Room '{}'. Total: {}",
                    req.getNickname(), result.getPlayerId(), roomCode, room != null ? room.getPlayers().size() : 0);
        } catch (RemoteException e) {
            log.error("[WS Gateway] joinRoom RMI error: {}", e.getMessage());
            sendError(roomCode, "RMI communication error: " + e.getMessage());
        }
    }

    @MessageMapping("/room.ready")
    public void toggleReady(@Payload ReadyToggleRequest req) {
        String roomCode = req.getRoomCode() != null ? req.getRoomCode().trim().toUpperCase() : "";
        try {
            log.info("[WS Gateway] /room.ready for player '{}' in room '{}' -> ready={}",
                    req.getPlayerId(), roomCode, req.isReady());
            boolean ok = quizRemote.setPlayerReady(roomCode, req.getPlayerId(), req.isReady());
            if (!ok) {
                sendError(roomCode, "Could not update ready status: Player not found.");
                return;
            }

            RoomDTO updatedRoom = quizRemote.getRoom(roomCode);
            String topic = "/topic/room/" + roomCode;

            // Broadcast PLAYER_READY event
            messagingTemplate.convertAndSend(topic, wrapEvent("PLAYER_READY", Map.of(
                    "playerId", req.getPlayerId(),
                    "ready", req.isReady(),
                    "room", updatedRoom != null ? updatedRoom : Map.of()
            )));

            // Broadcast ROOM_UPDATED event with full player state
            if (updatedRoom != null) {
                messagingTemplate.convertAndSend(topic, wrapEvent("ROOM_UPDATED", Map.of("room", updatedRoom)));
            }
        } catch (RemoteException e) {
            log.error("[WS Gateway] setPlayerReady error: {}", e.getMessage());
            sendError(roomCode, "Error setting ready status: " + e.getMessage());
        }
    }

    @MessageMapping("/room.leave")
    public void leaveRoom(@Payload LeaveRoomRequest req) {
        String roomCode = req.getRoomCode() != null ? req.getRoomCode().trim().toUpperCase() : "";
        try {
            log.info("[WS Gateway] /room.leave player '{}' leaving room '{}'", req.getPlayerId(), roomCode);
            quizRemote.leaveRoom(roomCode, req.getPlayerId());

            RoomDTO updatedRoom = quizRemote.getRoom(roomCode);
            String topic = "/topic/room/" + roomCode;

            messagingTemplate.convertAndSend(topic, wrapEvent("PLAYER_LEFT", Map.of(
                    "playerId", req.getPlayerId(),
                    "reason", "Player left the room",
                    "room", updatedRoom != null ? updatedRoom : Map.of()
            )));

            if (updatedRoom != null) {
                messagingTemplate.convertAndSend(topic, wrapEvent("ROOM_UPDATED", Map.of("room", updatedRoom)));
            }
        } catch (RemoteException e) {
            log.error("[WS Gateway] leaveRoom error: {}", e.getMessage());
            sendError(roomCode, "Error leaving room: " + e.getMessage());
        }
    }

    @MessageMapping("/room.kick")
    public void kickPlayer(@Payload RemovePlayerRequest req) {
        removePlayer(req);
    }

    @MessageMapping("/room.remove")
    public void removePlayer(@Payload RemovePlayerRequest req) {
        String roomCode = req.getRoomCode() != null ? req.getRoomCode().trim().toUpperCase() : "";
        try {
            log.info("[WS Gateway] /room.remove: host '{}' removing player '{}' from room '{}'",
                    req.getHostPlayerId(), req.getTargetPlayerId(), roomCode);

            boolean removed = quizRemote.removePlayer(roomCode, req.getHostPlayerId(), req.getTargetPlayerId());
            if (!removed) {
                sendError(roomCode, "Failed to remove player: player not found or already left.");
                return;
            }

            RoomDTO updatedRoom = quizRemote.getRoom(roomCode);
            String topic = "/topic/room/" + roomCode;

            // Broadcast PLAYER_LEFT with kicked flag
            messagingTemplate.convertAndSend(topic, wrapEvent("PLAYER_LEFT", Map.of(
                    "playerId", req.getTargetPlayerId(),
                    "kicked", true,
                    "reason", "Removed by host",
                    "room", updatedRoom != null ? updatedRoom : Map.of()
            )));

            if (updatedRoom != null) {
                messagingTemplate.convertAndSend(topic, wrapEvent("ROOM_UPDATED", Map.of("room", updatedRoom)));
            }
        } catch (RemoteException e) {
            log.error("[WS Gateway] removePlayer error: {}", e.getMessage());
            sendError(roomCode, "Cannot remove player: " + e.getMessage());
        }
    }

    @MessageMapping("/room.reconnect")
    public void reconnectPlayer(@Payload Map<String, String> payload, org.springframework.messaging.simp.SimpMessageHeaderAccessor headerAccessor) {
        String roomCode = payload.get("roomCode");
        String playerId = payload.get("playerId");
        if (roomCode != null) roomCode = roomCode.trim().toUpperCase();

        try {
            log.info("[WS Gateway] /room.reconnect: player '{}' reconnecting to room '{}'", playerId, roomCode);
            boolean ok = quizRemote.setPlayerConnected(roomCode, playerId, true);
            if (ok && headerAccessor != null && headerAccessor.getSessionId() != null) {
                com.devclash.gateway.listener.WebSocketEventListener.registerSession(
                        headerAccessor.getSessionId(), roomCode, playerId);
            }
            RoomDTO updatedRoom = quizRemote.getRoom(roomCode);
            String topic = "/topic/room/" + roomCode;
            messagingTemplate.convertAndSend(topic, wrapEvent("PLAYER_RECONNECTED", Map.of(
                    "playerId", playerId,
                    "connected", true,
                    "room", updatedRoom != null ? updatedRoom : Map.of()
            )));
            if (updatedRoom != null) {
                messagingTemplate.convertAndSend(topic, wrapEvent("ROOM_UPDATED", Map.of("room", updatedRoom)));
            }
        } catch (RemoteException e) {
            log.error("[WS Gateway] reconnectPlayer error: {}", e.getMessage());
        }
    }

    @MessageMapping("/game.start")
    public void startGame(@Payload Map<String, String> payload) {
        String roomCode = payload.get("roomCode");
        String hostPlayerId = payload.get("hostPlayerId");
        if (roomCode != null) roomCode = roomCode.trim().toUpperCase();

        try {
            log.info("[WS Gateway] /game.start requested by host '{}' for Room '{}'", hostPlayerId, roomCode);
            callbackHandler.ensureRegistered();
            boolean started = quizRemote.startGame(roomCode, hostPlayerId);
            if (!started) {
                sendError(roomCode, "Failed to start match.");
                return;
            }
            log.info("[WS Gateway] Match successfully started on RMI Server for Room '{}'", roomCode);
        } catch (RemoteException e) {
            log.error("[WS Gateway] startGame error: {}", e.getMessage());
            sendError(roomCode, e.getMessage());
        }
    }

    @MessageMapping("/game.answer")
    public void submitAnswer(@Payload AnswerSubmitRequest req) {
        String roomCode = req.getRoomCode() != null ? req.getRoomCode().trim().toUpperCase() : "";
        try {
            AnswerResultDTO result = quizRemote.submitAnswer(
                    roomCode,
                    req.getPlayerId(),
                    req.getQuestionIndex(),
                    req.getSelectedOption(),
                    req.getResponseTimeMs()
            );
            if (result != null) {
                result.setPlayerId(req.getPlayerId());
            }
            messagingTemplate.convertAndSend("/topic/room/" + roomCode, wrapEvent("ANSWER_ACK", result));
        } catch (RemoteException e) {
            log.error("[WS Gateway] submitAnswer error: {}", e.getMessage());
            sendError(roomCode, "Error submitting answer: " + e.getMessage());
        }
    }

    private void sendError(String roomCode, String message) {
        Map<String, String> err = new HashMap<>();
        err.put("error", message);
        if (roomCode != null && !roomCode.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/room/" + roomCode, wrapEvent("ERROR", err));
        } else {
            messagingTemplate.convertAndSend("/topic/errors", wrapEvent("ERROR", err));
        }
    }

    public static Map<String, Object> wrapEvent(String eventType, Object payload) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventType", eventType);
        map.put("timestamp", System.currentTimeMillis());
        map.put("payload", payload);
        return map;
    }
}
