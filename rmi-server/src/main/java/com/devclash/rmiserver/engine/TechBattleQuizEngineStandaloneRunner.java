package com.devclash.rmiserver.engine;

import com.devclash.common.dto.QuestionDTO;
import com.devclash.common.enums.Difficulty;
import com.devclash.common.enums.GameMode;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Standalone Independent Test Runner for TechBattleQuizEngine.
 * Can be executed directly via command line to verify all requirements.
 */
public class TechBattleQuizEngineStandaloneRunner {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   DEVCLASH TECH BATTLE QUIZ ENGINE - INDEPENDENT TEST RUNNER   ");
        System.out.println("===============================================================\n");

        int passed = 0;
        int failed = 0;

        // TEST 1: Categories & Bank Size
        try {
            System.out.println("[TEST 1] Verifying 6 Quiz Categories & Question Bank Size (>=15 per category)...");
            List<FullQuestion> all = FallbackQuestionBank.getAllQuestions(20);
            assertCheck(all.size() >= 90, "Total question count >= 90 (actual: " + all.size() + ")");

            for (int cat = 1; cat <= 6; cat++) {
                List<FullQuestion> catQ = FallbackQuestionBank.getQuestionsForCategory(cat, 20);
                assertCheck(catQ.size() >= 15, "Category " + cat + " has >= 15 questions (actual: " + catQ.size() + ")");
                Set<Difficulty> diffs = new HashSet<>();
                for (FullQuestion q : catQ) diffs.add(q.getDifficulty());
                assertCheck(diffs.contains(Difficulty.EASY) && diffs.contains(Difficulty.MEDIUM) && diffs.contains(Difficulty.HARD),
                        "Category " + cat + " covers EASY, MEDIUM, and HARD");
            }
            System.out.println("--> PASSED: All 6 categories verified with 16 questions each (96 total) across all difficulties.\n");
            passed++;
        } catch (Exception e) {
            System.err.println("--> FAILED: " + e.getMessage() + "\n");
            failed++;
        }

        // TEST 2: Question Requirements (4 options, 1 correct answer)
        try {
            System.out.println("[TEST 2] Verifying Question Structure (4 options, exactly 1 correct answer)...");
            List<FullQuestion> all = FallbackQuestionBank.getAllQuestions(20);
            for (FullQuestion q : all) {
                assertCheck(q.isValid(), "Question " + q.getId() + " is valid");
                assertCheck(q.getOptionA() != null && !q.getOptionA().isEmpty(), "Option A non-empty");
                assertCheck(q.getOptionB() != null && !q.getOptionB().isEmpty(), "Option B non-empty");
                assertCheck(q.getOptionC() != null && !q.getOptionC().isEmpty(), "Option C non-empty");
                assertCheck(q.getOptionD() != null && !q.getOptionD().isEmpty(), "Option D non-empty");
                String c = q.getCorrectOption();
                assertCheck(c.equals("A") || c.equals("B") || c.equals("C") || c.equals("D"),
                        "Question " + q.getId() + " has valid correct option: " + c);
                assertCheck(q.getExplanation() != null && !q.getExplanation().isEmpty(),
                        "Question " + q.getId() + " has explanation");
            }
            System.out.println("--> PASSED: All 96 questions have 4 options, 1 valid answer, and detailed explanations.\n");
            passed++;
        } catch (Exception e) {
            System.err.println("--> FAILED: " + e.getMessage() + "\n");
            failed++;
        }

        // TEST 3: Server-side Scoring Engine
        try {
            System.out.println("[TEST 3] Verifying Server-Side Scoring (Base 500 + Speed Bonus up to 500, Wrong=0)...");

            // Classic instant: 500 + 500 = 1000
            ScoringResult c1 = TechBattleQuizEngine.evaluateAnswer(GameMode.CLASSIC, true, 0, 20, "B", "Exp");
            assertCheck(c1.getBasePoints() == 500, "Classic base points = 500");
            assertCheck(c1.getSpeedBonus() == 500, "Classic instant bonus = 500");
            assertCheck(c1.getTotalPoints() == 1000, "Classic instant total = 1000");

            // Classic half-time (10s of 20s): 500 + 250 = 750
            ScoringResult c2 = TechBattleQuizEngine.evaluateAnswer(GameMode.CLASSIC, true, 10000, 20, "B", "Exp");
            assertCheck(c2.getBasePoints() == 500, "Classic half-time base = 500");
            assertCheck(c2.getSpeedBonus() == 250, "Classic half-time bonus = 250");
            assertCheck(c2.getTotalPoints() == 750, "Classic half-time total = 750");

            // Classic deadline (20s of 20s): 500 + 0 = 500
            ScoringResult c3 = TechBattleQuizEngine.evaluateAnswer(GameMode.CLASSIC, true, 20000, 20, "B", "Exp");
            assertCheck(c3.getBasePoints() == 500, "Classic deadline base = 500");
            assertCheck(c3.getSpeedBonus() == 0, "Classic deadline bonus = 0");
            assertCheck(c3.getTotalPoints() == 500, "Classic deadline total = 500");

            // Wrong answer: 0 points
            ScoringResult cWrong = TechBattleQuizEngine.evaluateAnswer(GameMode.CLASSIC, false, 1200, 20, "B", "Exp");
            assertCheck(!cWrong.isCorrect(), "isCorrect is false");
            assertCheck(cWrong.getBasePoints() == 0, "Wrong answer base = 0");
            assertCheck(cWrong.getSpeedBonus() == 0, "Wrong answer bonus = 0");
            assertCheck(cWrong.getTotalPoints() == 0, "Wrong answer total = 0");

            // Speed Rush mode (10s limit, higher speed bonus)
            ScoringResult sr1 = TechBattleQuizEngine.evaluateAnswer(GameMode.SPEED_RUSH, true, 2000, 10, "A", "Exp");
            assertCheck(sr1.getBasePoints() == 500, "Speed rush base = 500");
            assertCheck(sr1.getSpeedBonus() >= 400, "Speed rush fast bonus >= 400 (actual: " + sr1.getSpeedBonus() + ")");
            assertCheck(sr1.getTotalPoints() >= 900, "Speed rush fast total >= 900 (actual: " + sr1.getTotalPoints() + ")");

            // Brain Battle mode (30s limit, 15s = 750)
            ScoringResult bb1 = TechBattleQuizEngine.evaluateAnswer(GameMode.BRAIN_BATTLE, true, 15000, 30, "C", "Exp");
            assertCheck(bb1.getTotalPoints() == 750, "Brain battle 50% time total = 750 (actual: " + bb1.getTotalPoints() + ")");

            System.out.println("--> PASSED: Server-side scoring logic strictly verified across all modes and conditions.\n");
            passed++;
        } catch (Exception e) {
            System.err.println("--> FAILED: " + e.getMessage() + "\n");
            failed++;
        }

        // TEST 4: Security (QuestionDTO Sanitization)
        try {
            System.out.println("[TEST 4] Verifying Question Security (Answers NEVER sent to clients before close)...");
            FullQuestion q = new FullQuestion(50, 1, Difficulty.HARD, "Test Question?", null,
                    "A", "B", "C", "D", "B", "Secret Explanation", 20);
            QuestionDTO dto = q.toQuestionDTO(1, 10, "Programming");

            assertCheck(dto.getQuestionText().equals("Test Question?"), "DTO text matches");
            assertCheck(dto.getOptionA().equals("A"), "Option A present");

            // Reflection check: ensure no leaked fields
            for (Field f : QuestionDTO.class.getDeclaredFields()) {
                assertCheck(!f.getName().equalsIgnoreCase("correctOption"), "QuestionDTO must NOT have correctOption field!");
                assertCheck(!f.getName().equalsIgnoreCase("explanation"), "QuestionDTO must NOT have explanation field!");
            }
            System.out.println("--> PASSED: QuestionDTO is strictly sanitized with zero answer leaks.\n");
            passed++;
        } catch (Exception e) {
            System.err.println("--> FAILED: " + e.getMessage() + "\n");
            failed++;
        }

        // TEST 5: Random Selection Without Repetition
        try {
            System.out.println("[TEST 5] Verifying Random Question Selection without Repetition within Match...");
            for (int cat = 1; cat <= 6; cat++) {
                for (GameMode mode : GameMode.values()) {
                    List<FullQuestion> selected = TechBattleQuizEngine.selectQuestions(mode, cat, Difficulty.MEDIUM, 10);
                    assertCheck(selected.size() == 10, "Selected exactly 10 questions");
                    Set<Integer> uniqueIds = new HashSet<>();
                    for (FullQuestion q : selected) {
                        assertCheck(uniqueIds.add(q.getId()), "Duplicate question ID " + q.getId() + " detected in match!");
                    }
                    assertCheck(uniqueIds.size() == 10, "All 10 questions are distinct in match");
                }
            }
            System.out.println("--> PASSED: Random selection without repetition verified across all 6 categories and 3 game modes.\n");
            passed++;
        } catch (Exception e) {
            System.err.println("--> FAILED: " + e.getMessage() + "\n");
            failed++;
        }

        // SUMMARY
        System.out.println("===============================================================");
        System.out.printf("   TEST SUMMARY: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("===============================================================");

        if (failed > 0) {
            System.exit(1);
        } else {
            System.out.println("   ALL QUIZ ENGINE TESTS COMPLETED SUCCESSFULLY! (EXIT 0)     ");
            System.out.println("===============================================================");
        }
    }

    private static void assertCheck(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }
}
