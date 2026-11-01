package com.devclash.rmiserver.engine;

import com.devclash.common.dto.QuestionDTO;
import com.devclash.common.enums.Difficulty;
import com.devclash.common.enums.GameMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Independent Unit Test Suite for DevClash Tech Battle Quiz Engine.
 * Tests server-side scoring, game mode rules, question security, and non-repeating random selection.
 */
public class TechBattleQuizEngineTest {

    @Test
    @DisplayName("Scoring: Classic Battle - Correct answer base points (500) and speed bonus (0-500)")
    public void testClassicScoring() {
        // 1. Instant response (0 ms): Max 500 base + 500 bonus = 1000 pts
        ScoringResult instant = TechBattleQuizEngine.evaluateAnswer(
                GameMode.CLASSIC, true, 0, 20, "B", "Explanation here");
        assertTrue(instant.isCorrect());
        assertEquals(500, instant.getBasePoints(), "Base points must be 500");
        assertEquals(500, instant.getSpeedBonus(), "Instant response must award 500 speed bonus");
        assertEquals(1000, instant.getTotalPoints(), "Total points must be 1000");
        assertEquals("B", instant.getCorrectOption());
        assertEquals("Explanation here", instant.getExplanation());

        // 2. Half-time response (10 seconds out of 20 seconds): 500 base + 250 bonus = 750 pts
        ScoringResult halfTime = TechBattleQuizEngine.evaluateAnswer(
                GameMode.CLASSIC, true, 10000, 20, "B", "Explanation");
        assertTrue(halfTime.isCorrect());
        assertEquals(500, halfTime.getBasePoints());
        assertEquals(250, halfTime.getSpeedBonus(), "Half-time response must award 250 speed bonus");
        assertEquals(750, halfTime.getTotalPoints());

        // 3. Deadline response (20 seconds out of 20 seconds): 500 base + 0 bonus = 500 pts
        ScoringResult deadline = TechBattleQuizEngine.evaluateAnswer(
                GameMode.CLASSIC, true, 20000, 20, "B", "Explanation");
        assertTrue(deadline.isCorrect());
        assertEquals(500, deadline.getBasePoints());
        assertEquals(0, deadline.getSpeedBonus(), "Deadline response should award 0 speed bonus");
        assertEquals(500, deadline.getTotalPoints());

        // 4. Overtime response (25 seconds out of 20 seconds): 500 base + 0 bonus = 500 pts
        ScoringResult overtime = TechBattleQuizEngine.evaluateAnswer(
                GameMode.CLASSIC, true, 25000, 20, "B", "Explanation");
        assertTrue(overtime.isCorrect());
        assertEquals(500, overtime.getBasePoints());
        assertEquals(0, overtime.getSpeedBonus());
        assertEquals(500, overtime.getTotalPoints());
    }

    @Test
    @DisplayName("Scoring: Incorrect answer or no answer awards strictly 0 points")
    public void testIncorrectAndTimeoutScoring() {
        // Incorrect answer
        ScoringResult wrong = TechBattleQuizEngine.evaluateAnswer(
                GameMode.CLASSIC, false, 1500, 20, "C", "Correct explanation");
        assertFalse(wrong.isCorrect());
        assertEquals(0, wrong.getBasePoints(), "Incorrect answer must award 0 base points");
        assertEquals(0, wrong.getSpeedBonus(), "Incorrect answer must award 0 speed bonus");
        assertEquals(0, wrong.getTotalPoints(), "Incorrect answer must award 0 total points");
        assertEquals("C", wrong.getCorrectOption());
        assertEquals("Correct explanation", wrong.getExplanation());

        // Speed Rush incorrect
        ScoringResult wrongSpeed = TechBattleQuizEngine.evaluateAnswer(
                GameMode.SPEED_RUSH, false, 500, 10, "A", "Explanation");
        assertFalse(wrongSpeed.isCorrect());
        assertEquals(0, wrongSpeed.getTotalPoints());

        // Brain Battle incorrect
        ScoringResult wrongBrain = TechBattleQuizEngine.evaluateAnswer(
                GameMode.BRAIN_BATTLE, false, 8000, 30, "D", "Explanation");
        assertFalse(wrongBrain.isCorrect());
        assertEquals(0, wrongBrain.getTotalPoints());
    }

    @Test
    @DisplayName("Scoring: Speed Rush mode has 10s limit and higher speed bonus scaling")
    public void testSpeedRushScoring() {
        int timeLimit = TechBattleQuizEngine.getTimeLimitForMode(GameMode.SPEED_RUSH);
        assertEquals(10, timeLimit, "Speed Rush must have 10 seconds time limit");

        // Instant response: 1000 pts
        ScoringResult instant = TechBattleQuizEngine.evaluateAnswer(
                GameMode.SPEED_RUSH, true, 0, 10, "A", "Explanation");
        assertEquals(1000, instant.getTotalPoints());

        // 2-second response (8s remaining = 80% time): High speed bonus
        ScoringResult fast = TechBattleQuizEngine.evaluateAnswer(
                GameMode.SPEED_RUSH, true, 2000, 10, "A", "Explanation");
        assertTrue(fast.getSpeedBonus() >= 400, "Fast response in Speed Rush should receive high speed bonus");
        assertTrue(fast.getTotalPoints() >= 900);

        // 10-second response: 500 pts
        ScoringResult late = TechBattleQuizEngine.evaluateAnswer(
                GameMode.SPEED_RUSH, true, 10000, 10, "A", "Explanation");
        assertEquals(500, late.getTotalPoints());
    }

    @Test
    @DisplayName("Scoring: Brain Battle mode has 30s limit and full scale speed bonus")
    public void testBrainBattleScoring() {
        int timeLimit = TechBattleQuizEngine.getTimeLimitForMode(GameMode.BRAIN_BATTLE);
        assertEquals(30, timeLimit, "Brain Battle must have 30 seconds time limit");

        // 15 seconds response (half of 30s): 500 base + 250 bonus = 750 pts
        ScoringResult half = TechBattleQuizEngine.evaluateAnswer(
                GameMode.BRAIN_BATTLE, true, 15000, 30, "C", "Explanation");
        assertEquals(500, half.getBasePoints());
        assertEquals(250, half.getSpeedBonus());
        assertEquals(750, half.getTotalPoints());
    }

    @Test
    @DisplayName("Question Bank: All 6 categories exist with >=15 quality questions covering EASY, MEDIUM, HARD")
    public void testAllCategoriesAndDifficulties() {
        List<FullQuestion> allQuestions = FallbackQuestionBank.getAllQuestions(20);
        assertTrue(allQuestions.size() >= 90, "Total question bank must contain at least 90 questions (found " + allQuestions.size() + ")");

        // Check categories 1 through 6
        for (int catId = 1; catId <= 6; catId++) {
            List<FullQuestion> catQuestions = FallbackQuestionBank.getQuestionsForCategory(catId, 20);
            assertTrue(catQuestions.size() >= 15,
                    "Category " + catId + " must contain at least 15 questions (found " + catQuestions.size() + ")");

            Set<Difficulty> difficulties = new HashSet<>();
            for (FullQuestion q : catQuestions) {
                difficulties.add(q.getDifficulty());
            }

            assertTrue(difficulties.contains(Difficulty.EASY), "Category " + catId + " must contain EASY questions");
            assertTrue(difficulties.contains(Difficulty.MEDIUM), "Category " + catId + " must contain MEDIUM questions");
            assertTrue(difficulties.contains(Difficulty.HARD), "Category " + catId + " must contain HARD questions");
        }
    }

    @Test
    @DisplayName("Question Requirements: Each question has 4 non-null options and exactly 1 correct answer (A/B/C/D)")
    public void testQuestionStructureRequirements() {
        List<FullQuestion> all = FallbackQuestionBank.getAllQuestions(20);
        for (FullQuestion q : all) {
            assertTrue(q.isValid(), "Question ID " + q.getId() + " is invalid!");
            assertNotNull(q.getOptionA(), "Option A cannot be null");
            assertNotNull(q.getOptionB(), "Option B cannot be null");
            assertNotNull(q.getOptionC(), "Option C cannot be null");
            assertNotNull(q.getOptionD(), "Option D cannot be null");
            assertFalse(q.getOptionA().trim().isEmpty());
            assertFalse(q.getOptionB().trim().isEmpty());
            assertFalse(q.getOptionC().trim().isEmpty());
            assertFalse(q.getOptionD().trim().isEmpty());

            String corr = q.getCorrectOption();
            assertNotNull(corr);
            assertTrue(corr.equals("A") || corr.equals("B") || corr.equals("C") || corr.equals("D"),
                    "Correct option must be A, B, C, or D (got '" + corr + "')");
            assertNotNull(q.getExplanation(), "Explanation should not be null for question " + q.getId());
        }
    }

    @Test
    @DisplayName("Security: QuestionDTO never exposes correctOption or explanation to players")
    public void testQuestionDTOSecurity() {
        FullQuestion serverQuestion = new FullQuestion(999, 1, Difficulty.HARD,
                "What is the output?", "System.out.println(1);",
                "1", "0", "Error", "Null", "A", "Deep secret explanation", 20);

        QuestionDTO clientDTO = serverQuestion.toQuestionDTO(1, 10, "Programming");

        // Verify public getters
        assertEquals(999, clientDTO.getQuestionId());
        assertEquals("What is the output?", clientDTO.getQuestionText());
        assertEquals("System.out.println(1);", clientDTO.getCodeSnippet());
        assertEquals("1", clientDTO.getOptionA());
        assertEquals("0", clientDTO.getOptionB());
        assertEquals("Error", clientDTO.getOptionC());
        assertEquals("Null", clientDTO.getOptionD());
        assertEquals(20, clientDTO.getTimeLimitSec());

        // Reflective verification: QuestionDTO must NOT have correctOption or explanation fields
        Field[] fields = QuestionDTO.class.getDeclaredFields();
        for (Field f : fields) {
            assertNotEquals("correctOption", f.getName(), "Security violation: QuestionDTO exposes correctOption field!");
            assertNotEquals("explanation", f.getName(), "Security violation: QuestionDTO exposes explanation field!");
            assertNotEquals("answerKey", f.getName(), "Security violation: QuestionDTO exposes answerKey field!");
        }
    }

    @Test
    @DisplayName("Selection: Questions are randomly selected without repetition within a match")
    public void testRandomSelectionWithoutRepetition() {
        // Run test across multiple categories and game modes
        for (int catId = 1; catId <= 6; catId++) {
            for (GameMode mode : GameMode.values()) {
                List<FullQuestion> selected = TechBattleQuizEngine.selectQuestions(mode, catId, Difficulty.MEDIUM, 10);
                assertEquals(10, selected.size(), "Match must select exactly 10 questions");

                Set<Integer> uniqueIds = new HashSet<>();
                for (FullQuestion q : selected) {
                    boolean added = uniqueIds.add(q.getId());
                    assertTrue(added, "Duplicate question ID " + q.getId() + " detected in match for mode " + mode + "!");
                }
                assertEquals(10, uniqueIds.size(), "All 10 questions in match must be distinct");

                // Verify time limit corresponds to game mode
                int expectedTime = TechBattleQuizEngine.getTimeLimitForMode(mode);
                for (FullQuestion q : selected) {
                    assertEquals(expectedTime, q.getTimeLimitSec(), "Time limit on question must match game mode");
                }
            }
        }
    }

    @Test
    @DisplayName("Game Mode: Brain Battle prioritizes code snippets and challenging questions")
    public void testBrainBattleCodePriority() {
        List<FullQuestion> brainQuestions = TechBattleQuizEngine.selectQuestions(
                GameMode.BRAIN_BATTLE, 1, Difficulty.HARD, 10);
        assertEquals(10, brainQuestions.size());

        int codeSnippetCount = 0;
        for (FullQuestion q : brainQuestions) {
            if (q.getCodeSnippet() != null && !q.getCodeSnippet().trim().isEmpty()) {
                codeSnippetCount++;
            }
        }

        assertTrue(codeSnippetCount >= 3,
                "Brain Battle must prioritize code snippets / debugging questions (found " + codeSnippetCount + ")");
    }
}
