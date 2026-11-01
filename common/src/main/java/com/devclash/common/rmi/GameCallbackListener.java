package com.devclash.common.rmi;

import com.devclash.common.dto.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote Callback Interface implemented and exported by the Spring Boot Gateway.
 * Enables the RMI Quiz Server to notify the Gateway asynchronously of game state changes.
 */
public interface GameCallbackListener extends Remote {

    /**
     * Called when a new player joins the lobby.
     */
    void onPlayerJoined(String roomCode, PlayerDTO player) throws RemoteException;

    /**
     * Called when a player leaves or forfeits the room.
     */
    void onPlayerLeft(String roomCode, String playerId, String reason) throws RemoteException;

    /**
     * Called when a player's ready status or connection status changes.
     */
    void onPlayerReadyChanged(String roomCode, String playerId, boolean isReady, boolean isConnected)
            throws RemoteException;

    /**
     * Called when the countdown to start the match begins (e.g. 3, 2, 1).
     */
    void onGameStarting(String roomCode, int countdownSeconds) throws RemoteException;

    /**
     * Called when a new question round begins with a fresh question and active timer.
     */
    void onQuestionStarted(String roomCode, QuestionDTO question, int questionIndex,
                           int totalQuestions, int timeLimitSec) throws RemoteException;

    /**
     * Periodic 1-second countdown tick for the active question.
     */
    void onTimerTick(String roomCode, int secondsRemaining) throws RemoteException;

    /**
     * Called when any player submits an answer during an active question.
     */
    void onAnswerSubmitted(String roomCode, String playerId, String nickname, int questionIndex,
                           int totalAnswered, int totalConnected) throws RemoteException;

    /**
     * Called when player score updates after an evaluated answer.
     */
    void onScoreUpdated(String roomCode, String playerId, int newScore, int streak, int pointsAdded)
            throws RemoteException;

    /**
     * Called when the question round finishes (timeout or all players answered).
     * Distributes the correct answer, explanations, and current round stats.
     */
    void onQuestionEnded(String roomCode, QuestionResultDTO result) throws RemoteException;

    /**
     * Called when the leaderboard standings change.
     */
    void onLeaderboardUpdated(String roomCode, List<PlayerScoreDTO> leaderboard) throws RemoteException;

    /**
     * Called when all questions are complete, announcing the winner and final podium.
     */
    void onGameFinished(String roomCode, GameSummaryDTO summary) throws RemoteException;

    /**
     * Called when the room is closed by the host or server cleanup.
     */
    void onRoomClosed(String roomCode, String reason) throws RemoteException;
}
