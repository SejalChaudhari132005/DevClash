package com.devclash.rmiserver.service;

import com.devclash.common.dto.*;
import com.devclash.common.enums.Difficulty;
import com.devclash.common.enums.GameMode;
import com.devclash.common.enums.RoomState;
import com.devclash.common.rmi.GameCallbackListener;
import com.devclash.common.rmi.QuizGameService;
import com.devclash.rmiserver.config.DatabaseConfig;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RMI Remote Object Implementation for DevClash.
 * Extends UnicastRemoteObject to enable remote dispatch over JRMP.
 */
public class QuizGameServiceImpl extends UnicastRemoteObject implements QuizGameService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, GameRoomState> activeRooms = new ConcurrentHashMap<>();

    public QuizGameServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public RoomDTO createRoom(String hostNickname, String hostAvatar, GameConfigDTO config, GameCallbackListener callback)
            throws RemoteException {
        String roomCode = generateUniqueRoomCode();
        String hostId = "p_" + UUID.randomUUID().toString().substring(0, 8);
        String hostToken = UUID.randomUUID().toString();

        if (config == null) {
            config = new GameConfigDTO(1, "Programming & Languages", Difficulty.MEDIUM, GameMode.CLASSIC);
        }

        GameRoomState room = new GameRoomState(roomCode, hostId, hostNickname, hostAvatar, hostToken, config, callback);
        activeRooms.put(roomCode, room);

        System.out.printf("[RMI Server] Room created: %s by Host: %s (%s)%n", roomCode, hostNickname, hostId);

        RoomDTO roomDTO = room.toRoomDTO();
        roomDTO.setHostSessionToken(hostToken);
        return roomDTO;
    }

    @Override
    public JoinResultDTO joinRoom(String roomCode, String playerNickname, String playerAvatar, GameCallbackListener callback)
            throws RemoteException {
        if (roomCode == null) {
            return JoinResultDTO.fail("Room code cannot be empty.");
        }
        roomCode = roomCode.trim().toUpperCase();
        GameRoomState room = activeRooms.get(roomCode);
        if (room == null) {
            return JoinResultDTO.fail("Room not found. Please verify the 6-digit code.");
        }

        if (room.state != RoomState.WAITING_LOBBY) {
            return JoinResultDTO.fail("Cannot join: Battle has already started or room is closed.");
        }

        String playerId = "p_" + UUID.randomUUID().toString().substring(0, 8);
        String sessionToken = UUID.randomUUID().toString();

        PlayerDTO player = new PlayerDTO(playerId, playerNickname, false, playerAvatar);
        boolean joined = room.addPlayer(player, sessionToken, callback);
        if (!joined) {
            return JoinResultDTO.fail("Could not join room (duplicate nickname or room full).");
        }

        System.out.printf("[RMI Server] Player joined: %s (%s) into Room: %s%n", playerNickname, playerId, roomCode);

        // Notify all registered callbacks
        room.notifyPlayerJoined(player);

        return JoinResultDTO.ok(playerId, sessionToken, room.toRoomDTO());
    }

    @Override
    public void setPlayerReady(String roomCode, String playerId, boolean ready) throws RemoteException {
        GameRoomState room = activeRooms.get(roomCode);
        if (room != null) {
            room.setPlayerReady(playerId, ready);
            room.notifyPlayerStatus(playerId, ready, true);
        }
    }

    @Override
    public void startGame(String roomCode, String hostPlayerId) throws RemoteException {
        GameRoomState room = activeRooms.get(roomCode);
        if (room == null) {
            throw new RemoteException("Room not found: " + roomCode);
        }
        if (!room.hostId.equals(hostPlayerId)) {
            throw new RemoteException("Only the host can start the game.");
        }

        room.state = RoomState.STARTING_COUNTDOWN;
        System.out.printf("[RMI Server] Starting battle for Room: %s%n", roomCode);
        room.notifyGameStarting(3);
    }

    @Override
    public AnswerResultDTO submitAnswer(String roomCode, String playerId, int questionIndex,
                                        String selectedOption, long responseTimeMs) throws RemoteException {
        GameRoomState room = activeRooms.get(roomCode);
        if (room == null) {
            return AnswerResultDTO.rejected("Room not found.");
        }
        return room.recordAnswer(playerId, questionIndex, selectedOption, responseTimeMs);
    }

    @Override
    public GameStateDTO getRoomState(String roomCode, String playerId) throws RemoteException {
        GameRoomState room = activeRooms.get(roomCode);
        if (room == null) {
            return null;
        }
        return room.toGameStateDTO(playerId);
    }

    @Override
    public ReconnectResultDTO reconnectPlayer(String roomCode, String playerId, String sessionToken,
                                              GameCallbackListener callback) throws RemoteException {
        GameRoomState room = activeRooms.get(roomCode);
        if (room == null) {
            return ReconnectResultDTO.fail("Room not found or expired.");
        }
        boolean ok = room.reconnectPlayer(playerId, sessionToken, callback);
        if (!ok) {
            return ReconnectResultDTO.fail("Invalid player session token.");
        }
        return ReconnectResultDTO.ok(room.toGameStateDTO(playerId));
    }

    @Override
    public void leaveRoom(String roomCode, String playerId) throws RemoteException {
        GameRoomState room = activeRooms.get(roomCode);
        if (room != null) {
            room.removePlayer(playerId);
            System.out.printf("[RMI Server] Player %s left Room: %s%n", playerId, roomCode);
            if (room.isEmpty()) {
                activeRooms.remove(roomCode);
                System.out.printf("[RMI Server] Room %s closed (all players left).%n", roomCode);
            }
        }
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
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
        } while (activeRooms.containsKey(code));
        return code;
    }

    // --- Inner class representing in-memory GameRoom state ---
    private static class GameRoomState {
        final String roomCode;
        final String hostId;
        final GameConfigDTO config;
        RoomState state = RoomState.WAITING_LOBBY;
        final Map<String, PlayerDTO> players = new LinkedHashMap<>();
        final Map<String, String> sessionTokens = new ConcurrentHashMap<>();
        final List<GameCallbackListener> callbacks = new ArrayList<>();

        GameRoomState(String roomCode, String hostId, String hostNickname, String hostAvatar,
                      String hostToken, GameConfigDTO config, GameCallbackListener callback) {
            this.roomCode = roomCode;
            this.hostId = hostId;
            this.config = config;
            PlayerDTO host = new PlayerDTO(hostId, hostNickname, true, hostAvatar);
            host.setReady(true);
            players.put(hostId, host);
            sessionTokens.put(hostId, hostToken);
            if (callback != null) {
                callbacks.add(callback);
            }
        }

        synchronized boolean addPlayer(PlayerDTO player, String token, GameCallbackListener callback) {
            for (PlayerDTO p : players.values()) {
                if (p.getNickname().equalsIgnoreCase(player.getNickname())) {
                    return false; // Duplicate nickname
                }
            }
            players.put(player.getPlayerId(), player);
            sessionTokens.put(player.getPlayerId(), token);
            if (callback != null && !callbacks.contains(callback)) {
                callbacks.add(callback);
            }
            return true;
        }

        synchronized void setPlayerReady(String playerId, boolean ready) {
            PlayerDTO p = players.get(playerId);
            if (p != null) {
                p.setReady(ready);
            }
        }

        synchronized void removePlayer(String playerId) {
            PlayerDTO p = players.remove(playerId);
            sessionTokens.remove(playerId);
            if (p != null) {
                notifyPlayerLeft(playerId, "Player left the room");
            }
        }

        synchronized boolean reconnectPlayer(String playerId, String token, GameCallbackListener callback) {
            String existingToken = sessionTokens.get(playerId);
            if (existingToken != null && existingToken.equals(token)) {
                PlayerDTO p = players.get(playerId);
                if (p != null) {
                    p.setConnected(true);
                    if (callback != null && !callbacks.contains(callback)) {
                        callbacks.add(callback);
                    }
                    notifyPlayerStatus(playerId, p.isReady(), true);
                    return true;
                }
            }
            return false;
        }

        boolean isEmpty() {
            return players.isEmpty();
        }

        RoomDTO toRoomDTO() {
            RoomDTO dto = new RoomDTO(roomCode, hostId, players.containsKey(hostId) ? players.get(hostId).getNickname() : "Host", config);
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
            return stateDTO;
        }

        AnswerResultDTO recordAnswer(String playerId, int qIndex, String option, long timeMs) {
            PlayerDTO p = players.get(playerId);
            if (p == null) {
                return AnswerResultDTO.rejected("Player not in room.");
            }
            // Basic evaluation placeholder for initialization phase
            return AnswerResultDTO.evaluated(true, 1000, p.getScore() + 1000, 1, timeMs);
        }

        void notifyPlayerJoined(PlayerDTO player) {
            dispatchCallback(cb -> cb.onPlayerJoined(roomCode, player));
        }

        void notifyPlayerLeft(String playerId, String reason) {
            dispatchCallback(cb -> cb.onPlayerLeft(roomCode, playerId, reason));
        }

        void notifyPlayerStatus(String playerId, boolean ready, boolean connected) {
            dispatchCallback(cb -> cb.onPlayerReadyChanged(roomCode, playerId, ready, connected));
        }

        void notifyGameStarting(int countdown) {
            dispatchCallback(cb -> cb.onGameStarting(roomCode, countdown));
        }

        private void dispatchCallback(CallbackConsumer action) {
            synchronized (callbacks) {
                Iterator<GameCallbackListener> it = callbacks.iterator();
                while (it.hasNext()) {
                    GameCallbackListener cb = it.next();
                    try {
                        action.accept(cb);
                    } catch (RemoteException e) {
                        System.err.println("[RMI Server] Remote callback listener stale or failed; removing: " + e.getMessage());
                        it.remove();
                    }
                }
            }
        }
    }

    @FunctionalInterface
    private interface CallbackConsumer {
        void accept(GameCallbackListener listener) throws RemoteException;
    }
}
