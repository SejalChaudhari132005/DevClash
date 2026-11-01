package com.devclash.common.rmi;

import com.devclash.common.dto.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Main Java RMI Remote Interface exposed by the Quiz Server process.
 * All distributed game room, question, answer, and scoring methods are declared here.
 */
public interface QuizGameService extends Remote {

    /**
     * Creates a new multiplayer game room on the RMI server.
     */
    RoomDTO createRoom(String hostNickname, String hostAvatar, GameConfigDTO config, GameCallbackListener callback)
            throws RemoteException;

    /**
     * Joins an existing game room using the 6-digit room code.
     */
    JoinResultDTO joinRoom(String roomCode, String playerNickname, String playerAvatar, GameCallbackListener callback)
            throws RemoteException;

    /**
     * Updates player readiness in the lobby.
     */
    void setPlayerReady(String roomCode, String playerId, boolean ready)
            throws RemoteException;

    /**
     * Starts the quiz game. Only permitted by the host player.
     */
    void startGame(String roomCode, String hostPlayerId)
            throws RemoteException;

    /**
     * Submits an answer choice ('A', 'B', 'C', 'D') for the current question.
     */
    AnswerResultDTO submitAnswer(String roomCode, String playerId, int questionIndex,
                                 String selectedOption, long responseTimeMs)
            throws RemoteException;

    /**
     * Fetches current snapshot state of the room.
     */
    GameStateDTO getRoomState(String roomCode, String playerId)
            throws RemoteException;

    /**
     * Reconnects a player who was disconnected, restoring their session.
     */
    ReconnectResultDTO reconnectPlayer(String roomCode, String playerId, String sessionToken,
                                       GameCallbackListener callback)
            throws RemoteException;

    /**
     * Leaves the game room voluntarily.
     */
    void leaveRoom(String roomCode, String playerId)
            throws RemoteException;

    /**
     * Liveness check to verify RMI connection health.
     */
    boolean ping() throws RemoteException;
}
