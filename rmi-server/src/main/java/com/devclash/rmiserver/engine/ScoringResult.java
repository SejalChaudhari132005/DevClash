package com.devclash.rmiserver.engine;

import java.io.Serializable;

/**
 * Encapsulates the complete server-evaluated scoring outcome for a submitted answer.
 */
public class ScoringResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean correct;
    private final int basePoints;
    private final int speedBonus;
    private final int totalPoints;
    private final long responseTimeMs;
    private final String correctOption;
    private final String explanation;

    public ScoringResult(boolean correct, int basePoints, int speedBonus, int totalPoints,
                         long responseTimeMs, String correctOption, String explanation) {
        this.correct = correct;
        this.basePoints = basePoints;
        this.speedBonus = speedBonus;
        this.totalPoints = totalPoints;
        this.responseTimeMs = responseTimeMs;
        this.correctOption = correctOption;
        this.explanation = explanation;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getBasePoints() {
        return basePoints;
    }

    public int getSpeedBonus() {
        return speedBonus;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public String getExplanation() {
        return explanation;
    }

    @Override
    public String toString() {
        return "ScoringResult{" +
                "correct=" + correct +
                ", basePoints=" + basePoints +
                ", speedBonus=" + speedBonus +
                ", totalPoints=" + totalPoints +
                ", responseTimeMs=" + responseTimeMs +
                ", correctOption='" + correctOption + '\'' +
                '}';
    }
}
