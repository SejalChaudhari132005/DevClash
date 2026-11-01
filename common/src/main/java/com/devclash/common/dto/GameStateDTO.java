package com.devclash.common.dto;

import com.devclash.common.enums.RoomState;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Snapshot of complete game state for re-sync on client reconnect.
 */
public class GameStateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roomCode;
    private RoomState state;
    private GameConfigDTO config;
    private List<PlayerDTO> players;
    private QuestionDTO currentQuestion;
    private int currentQuestionIndex;
    private int timeRemainingSec;
    private boolean playerAlreadyAnswered;
    private String playerSelectedOption;
    private List<PlayerScoreDTO> currentLeaderboard;

    public GameStateDTO() {}

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public RoomState getState() {
        return state;
    }

    public void setState(RoomState state) {
        this.state = state;
    }

    public GameConfigDTO getConfig() {
        return config;
    }

    public void setConfig(GameConfigDTO config) {
        this.config = config;
    }

    public List<PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDTO> players) {
        this.players = players;
    }

    public QuestionDTO getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(QuestionDTO currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public int getTimeRemainingSec() {
        return timeRemainingSec;
    }

    public void setTimeRemainingSec(int timeRemainingSec) {
        this.timeRemainingSec = timeRemainingSec;
    }

    public boolean isPlayerAlreadyAnswered() {
        return playerAlreadyAnswered;
    }

    public void setPlayerAlreadyAnswered(boolean playerAlreadyAnswered) {
        this.playerAlreadyAnswered = playerAlreadyAnswered;
    }

    public String getPlayerSelectedOption() {
        return playerSelectedOption;
    }

    public void setPlayerSelectedOption(String playerSelectedOption) {
        this.playerSelectedOption = playerSelectedOption;
    }

    public List<PlayerScoreDTO> getCurrentLeaderboard() {
        return currentLeaderboard;
    }

    public void setCurrentLeaderboard(List<PlayerScoreDTO> currentLeaderboard) {
        this.currentLeaderboard = currentLeaderboard;
    }
}
