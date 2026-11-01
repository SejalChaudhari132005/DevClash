package com.devclash.rmiserver.engine;

import com.devclash.common.enums.Difficulty;
import com.devclash.common.enums.GameMode;
import com.devclash.rmiserver.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Core Quiz Engine for DevClash Tech Battle.
 *
 * Responsibilities:
 * - Server-side answer evaluation and scoring (500 base points + up to 500 speed bonus).
 * - Random question selection without repetition within each match.
 * - Game Mode configuration:
 *     * CLASSIC: 10 questions, 20s per question, normal scoring.
 *     * SPEED RUSH: 10 questions, 10s per question, higher speed bonus.
 *     * BRAIN BATTLE: 10 questions, 30s per question, code output/debugging/logical focus.
 * - Strict verification that every question has 4 options and 1 correct answer.
 * - In-memory curated fallback repository to guarantee 100% testability and zero crashes.
 */
public class TechBattleQuizEngine {

    public static final int BASE_POINTS = 500;
    public static final int MAX_SPEED_BONUS = 500;

    /**
     * Calculates server-side score for a submitted answer based on game mode and response time.
     *
     * @param mode Game mode (CLASSIC, SPEED_RUSH, BRAIN_BATTLE)
     * @param isCorrect Whether the player's selected option matches the question's correct option
     * @param responseTimeMs Player's reaction time in milliseconds
     * @param timeLimitSec Total round time limit in seconds
     * @param correctOption The single correct option ('A', 'B', 'C', or 'D')
     * @param explanation Detailed explanation of the answer
     * @return ScoringResult containing points awarded, speed bonus, and answer explanation
     */
    public static ScoringResult evaluateAnswer(GameMode mode, boolean isCorrect, long responseTimeMs,
                                               int timeLimitSec, String correctOption, String explanation) {
        if (!isCorrect) {
            // Incorrect answer: strictly 0 points
            return new ScoringResult(false, 0, 0, 0, responseTimeMs, correctOption, explanation);
        }

        // Base points for any correct answer is 500
        int basePoints = BASE_POINTS;

        // Effective round duration in milliseconds
        long timeLimitMs = (timeLimitSec > 0 ? timeLimitSec : 20) * 1000L;
        long clampedTime = Math.max(0, Math.min(responseTimeMs, timeLimitMs));

        // Remaining time ratio: 1.0 (instantaneous) down to 0.0 (at deadline)
        double remainingRatio = Math.max(0.0, 1.0 - ((double) clampedTime / (double) timeLimitMs));

        int speedBonus;
        if (mode == GameMode.SPEED_RUSH) {
            // SPEED RUSH: 10s per question, higher speed bonus rewarding rapid responses
            speedBonus = (int) Math.min(MAX_SPEED_BONUS, Math.round(MAX_SPEED_BONUS * Math.pow(remainingRatio, 0.75)));
        } else {
            // CLASSIC & BRAIN BATTLE: Linear speed bonus up to 500 points
            speedBonus = (int) Math.round(MAX_SPEED_BONUS * remainingRatio);
        }

        // Clamp speed bonus within [0, 500]
        speedBonus = Math.max(0, Math.min(MAX_SPEED_BONUS, speedBonus));

        int totalPoints = basePoints + speedBonus;

        return new ScoringResult(true, basePoints, speedBonus, totalPoints, responseTimeMs, correctOption, explanation);
    }

    /**
     * Determines default question time limit in seconds for a given game mode.
     */
    public static int getTimeLimitForMode(GameMode mode) {
        if (mode == GameMode.SPEED_RUSH) return 10;
        if (mode == GameMode.BRAIN_BATTLE) return 30;
        return 20; // CLASSIC default
    }

    /**
     * Randomly selects questions without repetition for a match.
     *
     * @param mode Game mode
     * @param categoryId Category ID (1 to 6)
     * @param difficulty Selected difficulty filter
     * @param totalQuestions Number of questions needed (default 10)
     * @return List of validated FullQuestion objects with no duplicates
     */
    public static List<FullQuestion> selectQuestions(GameMode mode, int categoryId, Difficulty difficulty, int totalQuestions) {
        if (totalQuestions <= 0) totalQuestions = 10;
        int timeLimitSec = getTimeLimitForMode(mode);

        List<FullQuestion> selected = new ArrayList<>();
        Set<Integer> selectedIds = new HashSet<>();

        // 1. Attempt loading from MySQL database
        if (DatabaseConfig.isAvailable()) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql;
                if (mode == GameMode.BRAIN_BATTLE) {
                    // Prioritize code snippets, debugging, and harder reasoning questions
                    sql = "SELECT id, category_id, difficulty, question_text, code_snippet, " +
                            "option_a, option_b, option_c, option_d, correct_option, explanation " +
                            "FROM questions WHERE category_id = ? " +
                            "ORDER BY (code_snippet IS NOT NULL) DESC, RAND() LIMIT ?";
                } else {
                    sql = "SELECT id, category_id, difficulty, question_text, code_snippet, " +
                            "option_a, option_b, option_c, option_d, correct_option, explanation " +
                            "FROM questions WHERE category_id = ? " +
                            "ORDER BY RAND() LIMIT ?";
                }

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, categoryId > 0 ? categoryId : 1);
                    stmt.setInt(2, totalQuestions);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            if (selectedIds.add(id)) {
                                FullQuestion q = new FullQuestion();
                                q.setId(id);
                                q.setCategoryId(rs.getInt("category_id"));
                                String diffStr = rs.getString("difficulty");
                                try {
                                    q.setDifficulty(Difficulty.valueOf(diffStr.toUpperCase()));
                                } catch (Exception e) {
                                    q.setDifficulty(Difficulty.MEDIUM);
                                }
                                q.setQuestionText(rs.getString("question_text"));
                                q.setCodeSnippet(rs.getString("code_snippet"));
                                q.setOptionA(rs.getString("option_a"));
                                q.setOptionB(rs.getString("option_b"));
                                q.setOptionC(rs.getString("option_c"));
                                q.setOptionD(rs.getString("option_d"));
                                q.setCorrectOption(rs.getString("correct_option"));
                                q.setExplanation(rs.getString("explanation"));
                                q.setTimeLimitSec(timeLimitSec);

                                if (q.isValid()) {
                                    selected.add(q);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[TechBattleQuizEngine] Database query error: " + e.getMessage() + ". Using curated fallback.");
            }
        }

        // 2. If insufficient questions were retrieved, supplement from in-memory fallback without repetition
        if (selected.size() < totalQuestions) {
            List<FullQuestion> fallbacks = FallbackQuestionBank.getQuestionsForCategory(categoryId, timeLimitSec);
            Collections.shuffle(fallbacks);
            for (FullQuestion fq : fallbacks) {
                if (selected.size() >= totalQuestions) break;
                if (selectedIds.add(fq.getId())) {
                    selected.add(fq);
                }
            }
        }

        // 3. Final verification: ensure all have exactly 4 options and 1 correct answer
        for (FullQuestion q : selected) {
            if (!q.isValid()) {
                throw new IllegalStateException("Question ID " + q.getId() + " fails 4-option validation requirement!");
            }
        }

        return selected;
    }
}
