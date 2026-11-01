package com.devclash.common.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Final match summary and podium rankings.
 */
public class GameSummaryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String matchUuid;
    private String roomCode;
    private String winnerNickname;
    private int winnerScore;
    private List<PlayerScoreDTO> finalPodium; // Top 3
    private List<PlayerScoreDTO> fullRankings;
    private int totalQuestions;
    private String gameMode;
    private String difficulty;
    private String categoryName;
    private long totalDurationSec;

    public GameSummaryDTO() {}

    public String getMatchUuid() {
        return matchUuid;
    }

    public void setMatchUuid(String matchUuid) {
        this.matchUuid = matchUuid;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getWinnerNickname() {
        return winnerNickname;
    }

    public void setWinnerNickname(String winnerNickname) {
        this.winnerNickname = winnerNickname;
    }

    public int getWinnerScore() {
        return winnerScore;
    }

    public void setWinnerScore(int winnerScore) {
        this.winnerScore = winnerScore;
    }

    public List<PlayerScoreDTO> getFinalPodium() {
        return finalPodium;
    }

    public void setFinalPodium(List<PlayerScoreDTO> finalPodium) {
        this.finalPodium = finalPodium;
    }

    public List<PlayerScoreDTO> getFullRankings() {
        return fullRankings;
    }

    public void setFullRankings(List<PlayerScoreDTO> fullRankings) {
        this.fullRankings = fullRankings;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public long getTotalDurationSec() {
        return totalDurationSec;
    }

    public void setTotalDurationSec(long totalDurationSec) {
        this.totalDurationSec = totalDurationSec;
    }
}
