package com.devclash.common.rmi;

import com.devclash.common.dto.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * DevClash Core Remote Interface extending java.rmi.Remote.
 * Defines remote procedure call (RPC) contracts between Gateway and RMI Quiz Server.
 */
public interface QuizRemote extends Remote {

    /**
     * Registers a remote callback listener (such as Spring Boot Gateway) for async game notifications.
     */
    void registerCallbackListener(String clientKey, GameCallbackListener listener) throws RemoteException;

    /**
     * Unregisters a remote callback listener.
     */
    void unregisterCallbackListener(String clientKey) throws RemoteException;

    /**
     * Creates a new quiz room with a unique 6-digit room code.
     */
    RoomDTO createRoom(String hostNickname, String hostAvatar, GameConfigDTO config) throws RemoteException;

    /**
     * Joins an existing quiz room using the 6-digit room code and player nickname.
     */
    JoinResultDTO joinRoom(String roomCode, String playerNickname, String playerAvatar) throws RemoteException;

    /**
     * Leaves a room voluntarily.
     */
    boolean leaveRoom(String roomCode, String playerId) throws RemoteException;

    /**
     * Toggles ready/not ready status for a player in the lobby.
     */
    boolean setPlayerReady(String roomCode, String playerId, boolean ready) throws RemoteException;

    /**
     * Starts the battle. Only permitted by the host player. Initiates authoritative match state machine.
     */
    boolean startGame(String roomCode, String hostPlayerId) throws RemoteException;

    /**
     * Fetches the complete room snapshot state.
     */
    GameStateDTO getRoomState(String roomCode, String playerId) throws RemoteException;

    /**
     * Fetches the active question for the given room (without answer key).
     */
    QuestionDTO getCurrentQuestion(String roomCode) throws RemoteException;

    /**
     * Fetches question result summary for a completed question round.
     */
    QuestionResultDTO getQuestionResult(String roomCode, int questionIndex) throws RemoteException;

    /**
     * Submits an answer choice ('A', 'B', 'C', 'D') for server-side validation and scoring.
     */
    AnswerResultDTO submitAnswer(String roomCode, String playerId, int questionIndex,
                                 String selectedOption, long responseTimeMs) throws RemoteException;

    /**
     * Retrieves the current sorted live leaderboard for the room.
     */
    List<PlayerScoreDTO> getLeaderboard(String roomCode) throws RemoteException;

    /**
     * Advances to the next question round.
     */
    QuestionDTO nextQuestion(String roomCode, String hostPlayerId) throws RemoteException;

    /**
     * Concludes the match, computes final podium standings, and archives summary to MySQL.
     */
    GameSummaryDTO endGame(String roomCode, String hostPlayerId) throws RemoteException;

    /**
     * Removes/kicks a player from the room. Only permitted by the host player.
     */
    boolean removePlayer(String roomCode, String hostPlayerId, String targetPlayerId) throws RemoteException;

    /**
     * Updates player online connection status (e.g. on WebSocket disconnect).
     */
    boolean setPlayerConnected(String roomCode, String playerId, boolean connected) throws RemoteException;

    /**
     * Fetches current RoomDTO details including all players.
     */
    RoomDTO getRoom(String roomCode) throws RemoteException;

    /**
     * Liveness check to verify RMI connection health.
     */
    boolean ping() throws RemoteException;
}
