package com.devclash.common.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * Result returned immediately to an individual player upon submitting an answer.
 */
public class AnswerResultDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean accepted;
    private String statusMessage;
    private boolean correct;
    private int pointsAwarded;
    private int currentTotalScore;
    private int currentStreak;
    private long responseTimeMs;

    private String correctOption;
    private String explanation;
    private int basePoints;
    private int speedBonus;
    private String playerId;

    public AnswerResultDTO() {}

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public static AnswerResultDTO rejected(String reason) {
        AnswerResultDTO res = new AnswerResultDTO();
        res.setAccepted(false);
        res.setStatusMessage(reason);
        return res;
    }

    public static AnswerResultDTO evaluated(boolean correct, int points, int totalScore, int streak, long timeMs) {
        return evaluated(correct, points, correct ? 500 : 0, correct ? Math.max(0, points - 500) : 0, totalScore, streak, timeMs, null, null);
    }

    public static AnswerResultDTO evaluated(boolean correct, int points, int basePoints, int speedBonus,
                                            int totalScore, int streak, long timeMs,
                                            String correctOption, String explanation) {
        AnswerResultDTO res = new AnswerResultDTO();
        res.setAccepted(true);
        res.setCorrect(correct);
        res.setPointsAwarded(points);
        res.setBasePoints(basePoints);
        res.setSpeedBonus(speedBonus);
        res.setCurrentTotalScore(totalScore);
        res.setCurrentStreak(streak);
        res.setResponseTimeMs(timeMs);
        res.setCorrectOption(correctOption);
        res.setExplanation(explanation);
        res.setStatusMessage(correct ? "Correct answer!" : "Incorrect answer!");
        return res;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(int pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public int getCurrentTotalScore() {
        return currentTotalScore;
    }

    public void setCurrentTotalScore(int currentTotalScore) {
        this.currentTotalScore = currentTotalScore;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getBasePoints() {
        return basePoints;
    }

    public void setBasePoints(int basePoints) {
        this.basePoints = basePoints;
    }

    public int getSpeedBonus() {
        return speedBonus;
    }

    public void setSpeedBonus(int speedBonus) {
        this.speedBonus = speedBonus;
    }
}
