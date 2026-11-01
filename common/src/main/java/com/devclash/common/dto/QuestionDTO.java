package com.devclash.common.dto;

import com.devclash.common.enums.Difficulty;
import java.io.Serial;
import java.io.Serializable;

/**
 * Question sent to clients during an active round.
 * Does NOT contain the correct answer or explanation to prevent client tampering.
 */
public class QuestionDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int questionId;
    private int questionIndex;
    private int totalQuestions;
    private String categoryName;
    private Difficulty difficulty;
    private String questionText;
    private String codeSnippet;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int timeLimitSec;

    public QuestionDTO() {}

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
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

    public int getTimeLimitSec() {
        return timeLimitSec;
    }

    public void setTimeLimitSec(int timeLimitSec) {
        this.timeLimitSec = timeLimitSec;
    }
}
