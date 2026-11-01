package com.devclash.rmiserver.engine;

import com.devclash.common.dto.QuestionDTO;
import com.devclash.common.enums.Difficulty;

import java.io.Serializable;
import java.util.Objects;

/**
 * Server-side full question model containing correct option and explanation.
 * Securely retained in RMI Server memory and never leaked directly to clients.
 */
public class FullQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int categoryId;
    private Difficulty difficulty;
    private String questionText;
    private String codeSnippet;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption; // 'A', 'B', 'C', 'D'
    private String explanation;
    private int timeLimitSec;

    public FullQuestion() {
        this.difficulty = Difficulty.MEDIUM;
        this.timeLimitSec = 20;
    }

    public FullQuestion(int id, int categoryId, Difficulty difficulty, String questionText,
                        String codeSnippet, String optionA, String optionB, String optionC,
                        String optionD, String correctOption, String explanation, int timeLimitSec) {
        this.id = id;
        this.categoryId = categoryId;
        this.difficulty = difficulty != null ? difficulty : Difficulty.MEDIUM;
        this.questionText = questionText;
        this.codeSnippet = codeSnippet;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption != null ? correctOption.trim().toUpperCase() : "A";
        this.explanation = explanation;
        this.timeLimitSec = timeLimitSec > 0 ? timeLimitSec : 20;
    }

    /**
     * Converts to a safe, sanitized QuestionDTO for transmission to players.
     * Guaranteed NOT to contain the correct answer or explanation.
     */
    public QuestionDTO toQuestionDTO(int index, int total, String categoryName) {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionId(this.id);
        dto.setQuestionIndex(index);
        dto.setTotalQuestions(total);
        dto.setCategoryName(categoryName != null ? categoryName : "Tech Battle");
        dto.setDifficulty(this.difficulty);
        dto.setQuestionText(this.questionText);
        dto.setCodeSnippet(this.codeSnippet);
        dto.setOptionA(this.optionA);
        dto.setOptionB(this.optionB);
        dto.setOptionC(this.optionC);
        dto.setOptionD(this.optionD);
        dto.setTimeLimitSec(this.timeLimitSec);
        return dto;
    }

    /**
     * Validates that this question satisfies all technical requirements:
     * - Exactly 4 non-empty options
     * - Exactly 1 valid correct option ('A', 'B', 'C', or 'D')
     */
    public boolean isValid() {
        if (questionText == null || questionText.trim().isEmpty()) return false;
        if (optionA == null || optionA.trim().isEmpty()) return false;
        if (optionB == null || optionB.trim().isEmpty()) return false;
        if (optionC == null || optionC.trim().isEmpty()) return false;
        if (optionD == null || optionD.trim().isEmpty()) return false;
        if (correctOption == null) return false;
        return correctOption.equals("A") || correctOption.equals("B") ||
               correctOption.equals("C") || correctOption.equals("D");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
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

    public int getTimeLimitSec() {
        return timeLimitSec;
    }

    public void setTimeLimitSec(int timeLimitSec) {
        this.timeLimitSec = timeLimitSec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullQuestion that = (FullQuestion) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
