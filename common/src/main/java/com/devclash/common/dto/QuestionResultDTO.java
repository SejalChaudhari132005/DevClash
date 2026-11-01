package com.devclash.common.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Broadcasted at the end of a question round revealing the answer key and updated rankings.
 */
public class QuestionResultDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int questionIndex;
    private String correctOption; // 'A', 'B', 'C', 'D'
    private String explanation;
    private Map<String, Integer> optionDistribution; // e.g. "A": 3, "B": 1, ...
    private List<PlayerScoreDTO> updatedLeaderboard;
    private int nextQuestionInSec;

    public QuestionResultDTO() {}

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
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

    public Map<String, Integer> getOptionDistribution() {
        return optionDistribution;
    }

    public void setOptionDistribution(Map<String, Integer> optionDistribution) {
        this.optionDistribution = optionDistribution;
    }

    public List<PlayerScoreDTO> getUpdatedLeaderboard() {
        return updatedLeaderboard;
    }

    public void setUpdatedLeaderboard(List<PlayerScoreDTO> updatedLeaderboard) {
        this.updatedLeaderboard = updatedLeaderboard;
    }

    public int getNextQuestionInSec() {
        return nextQuestionInSec;
    }

    public void setNextQuestionInSec(int nextQuestionInSec) {
        this.nextQuestionInSec = nextQuestionInSec;
    }
}
