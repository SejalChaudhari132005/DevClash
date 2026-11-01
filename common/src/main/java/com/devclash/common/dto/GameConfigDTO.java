package com.devclash.common.dto;

import com.devclash.common.enums.Difficulty;
import com.devclash.common.enums.GameMode;
import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration options selected by the room host.
 */
public class GameConfigDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int categoryId;
    private String categoryName;
    private Difficulty difficulty;
    private GameMode gameMode;
    private int totalQuestions;
    private int questionTimeLimitSec;

    public GameConfigDTO() {
        this.totalQuestions = 10;
        this.questionTimeLimitSec = 20;
        this.difficulty = Difficulty.MEDIUM;
        this.gameMode = GameMode.CLASSIC;
    }

    public GameConfigDTO(int categoryId, String categoryName, Difficulty difficulty, GameMode gameMode) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.difficulty = difficulty != null ? difficulty : Difficulty.MEDIUM;
        this.gameMode = gameMode != null ? gameMode : GameMode.CLASSIC;
        this.totalQuestions = 10;
        this.questionTimeLimitSec = computeDefaultTimeLimit(this.gameMode);
    }

    public static int computeDefaultTimeLimit(GameMode mode) {
        if (mode == GameMode.SPEED_RUSH) return 10;
        if (mode == GameMode.BRAIN_BATTLE) return 30;
        return 20;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getQuestionCount() {
        return totalQuestions;
    }

    public void setQuestionCount(int questionCount) {
        this.totalQuestions = questionCount;
    }

    public int getQuestionTimeLimitSec() {
        return questionTimeLimitSec;
    }

    public void setQuestionTimeLimitSec(int questionTimeLimitSec) {
        this.questionTimeLimitSec = questionTimeLimitSec;
    }
}
