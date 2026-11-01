package com.devclash.common.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * Details of a player participating in a DevClash game room.
 */
public class PlayerDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String playerId;
    private String nickname;
    private boolean host;
    private boolean ready;
    private boolean connected;
    private int score;
    private int currentStreak;
    private int rank;
    private int previousRank;
    private int correctAnswers;
    private int wrongAnswers;
    private long cumulativeResponseTimeMs;
    private String avatar;

    public PlayerDTO() {}

    public PlayerDTO(String playerId, String nickname, boolean host, String avatar) {
        this.playerId = playerId;
        this.nickname = nickname;
        this.host = host;
        this.ready = host; // Host is ready by default
        this.connected = true;
        this.score = 0;
        this.currentStreak = 0;
        this.rank = 1;
        this.previousRank = 1;
        this.correctAnswers = 0;
        this.wrongAnswers = 0;
        this.cumulativeResponseTimeMs = 0;
        this.avatar = avatar != null ? avatar : "terminal";
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

    public boolean isHost() {
        return host;
    }

    public boolean getHost() {
        return host;
    }

    public boolean getIsHost() {
        return host;
    }

    public void setHost(boolean host) {
        this.host = host;
    }

    public void setIsHost(boolean host) {
        this.host = host;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
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

    public long getCumulativeResponseTimeMs() {
        return cumulativeResponseTimeMs;
    }

    public void setCumulativeResponseTimeMs(long cumulativeResponseTimeMs) {
        this.cumulativeResponseTimeMs = cumulativeResponseTimeMs;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
