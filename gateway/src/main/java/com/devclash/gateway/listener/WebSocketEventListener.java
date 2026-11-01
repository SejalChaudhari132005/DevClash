package com.devclash.gateway.listener;

import com.devclash.common.dto.RoomDTO;
import com.devclash.common.rmi.QuizRemote;
import com.devclash.gateway.controller.WebSocketGameController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens to WebSocket lifecycle events such as client disconnections.
 * Updates central RMI state to mark player disconnected and broadcasts PLAYER_DISCONNECTED & ROOM_UPDATED.
 */
@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final QuizRemote quizRemote;
    private final SimpMessagingTemplate messagingTemplate;

    // Map of sessionId -> PlayerSession
    private static final Map<String, PlayerSession> activeSessions = new ConcurrentHashMap<>();

    public WebSocketEventListener(QuizRemote quizRemote, SimpMessagingTemplate messagingTemplate) {
        this.quizRemote = quizRemote;
        this.messagingTemplate = messagingTemplate;
    }

    public static void registerSession(String sessionId, String roomCode, String playerId) {
        if (sessionId != null && roomCode != null && playerId != null) {
            activeSessions.put(sessionId, new PlayerSession(roomCode, playerId));
            log.info("[WS Session] Registered session {} -> room={}, player={}", sessionId, roomCode, playerId);
        }
    }

    public static void unregisterSession(String sessionId) {
        if (sessionId != null) {
            activeSessions.remove(sessionId);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (sessionId == null) return;

        PlayerSession session = activeSessions.remove(sessionId);
        if (session != null) {
            log.warn("[WS Session] Player disconnected: session={}, room={}, player={}",
                    sessionId, session.roomCode, session.playerId);
            try {
                // Update player connection status in RMI Server
                quizRemote.setPlayerConnected(session.roomCode, session.playerId, false);
                RoomDTO updatedRoom = quizRemote.getRoom(session.roomCode);

                String topic = "/topic/room/" + session.roomCode;
                messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("PLAYER_DISCONNECTED", Map.of(
                        "playerId", session.playerId,
                        "connected", false,
                        "room", updatedRoom != null ? updatedRoom : Map.of()
                )));

                if (updatedRoom != null) {
                    messagingTemplate.convertAndSend(topic, WebSocketGameController.wrapEvent("ROOM_UPDATED", Map.of(
                            "room", updatedRoom
                    )));
                }
            } catch (RemoteException e) {
                log.error("[WS Session] Failed to update RMI server on disconnect: {}", e.getMessage());
            }
        }
    }

    public static class PlayerSession {
        public final String roomCode;
        public final String playerId;

        public PlayerSession(String roomCode, String playerId) {
            this.roomCode = roomCode;
            this.playerId = playerId;
        }
    }
}
