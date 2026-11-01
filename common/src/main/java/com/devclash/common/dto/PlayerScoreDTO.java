package com.devclash.common.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * Scoreboard entry for a player on the live leaderboard.
 * Carries points, streak, accuracy, correct count, response times, and rank movements.
 */
public class PlayerScoreDTO implements Serializable, Comparable<PlayerScoreDTO> {
    @Serial
    private static final long serialVersionUID = 1L;

    private String playerId;
    private String nickname;
    private String avatar;
    private int score;
    private int rank;
    private int previousRank;
    private int rankChange; // +1 moved up, -1 moved down, 0 unchanged
    private int streak;
    private int correctAnswers;
    private int wrongAnswers;
    private int totalAnswered;
    private int accuracyPercentage;
    private long cumulativeAnswerTimeMs;
    private long avgResponseTimeMs;
    private boolean answeredCurrent;
    private boolean lastAnswerCorrect;
    private int lastPointsGained;

    public PlayerScoreDTO() {}

    public PlayerScoreDTO(String playerId, String nickname, int score, int rank, int streak) {
        this.playerId = playerId;
        this.nickname = nickname;
        this.score = score;
        this.rank = rank;
        this.previousRank = rank;
        this.rankChange = 0;
        this.streak = streak;
        this.avatar = "terminal";
    }

    /**
     * Authoritative Distributed Tie-Breaking:
     * 1. Total points (descending)
     * 2. Total correct answers (descending)
     * 3. Cumulative answer time (ascending - faster is better)
     */
    @Override
    public int compareTo(PlayerScoreDTO other) {
        // 1. Total points (higher is better)
        if (this.score != other.score) {
            return Integer.compare(other.score, this.score);
        }
        // 2. Tie-break: Total correct answers (higher is better)
        if (this.correctAnswers != other.correctAnswers) {
            return Integer.compare(other.correctAnswers, this.correctAnswers);
        }
        // 3. Tie-break: Cumulative answer time (lower/faster is better)
        if (this.cumulativeAnswerTimeMs != other.cumulativeAnswerTimeMs) {
            return Long.compare(this.cumulativeAnswerTimeMs, other.cumulativeAnswerTimeMs);
        }
        // Deterministic fallback
        if (this.playerId != null && other.playerId != null) {
            return this.playerId.compareTo(other.playerId);
        }
        return 0;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar != null ? avatar : "terminal";
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getPreviousRank() {
        return previousRank;
    }

    public void setPreviousRank(int previousRank) {
        this.previousRank = previousRank;
    }

    public int getRankChange() {
        return rankChange;
    }

    public void setRankChange(int rankChange) {
        this.rankChange = rankChange;
    }

    public int getStreak() {
        return streak;
    }

    public int getCurrentStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public int getTotalAnswered() {
        return totalAnswered;
    }

    public void setTotalAnswered(int totalAnswered) {
        this.totalAnswered = totalAnswered;
    }

    public int getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(int accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }

    public long getCumulativeAnswerTimeMs() {
        return cumulativeAnswerTimeMs;
    }

    public void setCumulativeAnswerTimeMs(long cumulativeAnswerTimeMs) {
        this.cumulativeAnswerTimeMs = cumulativeAnswerTimeMs;
    }

    public long getAvgResponseTimeMs() {
        return avgResponseTimeMs;
    }

    public void setAvgResponseTimeMs(long avgResponseTimeMs) {
        this.avgResponseTimeMs = avgResponseTimeMs;
    }

    public boolean isAnsweredCurrent() {
        return answeredCurrent;
    }

    public void setAnsweredCurrent(boolean answeredCurrent) {
        this.answeredCurrent = answeredCurrent;
    }

    public boolean isLastAnswerCorrect() {
        return lastAnswerCorrect;
    }

    public void setLastAnswerCorrect(boolean lastAnswerCorrect) {
        this.lastAnswerCorrect = lastAnswerCorrect;
    }

    public int getLastPointsGained() {
        return lastPointsGained;
    }

    public void setLastPointsGained(int lastPointsGained) {
        this.lastPointsGained = lastPointsGained;
    }
}
