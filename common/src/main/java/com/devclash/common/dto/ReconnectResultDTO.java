package com.devclash.common.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * Result returned upon a player's reconnection attempt.
 */
public class ReconnectResultDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private GameStateDTO gameState;

    public ReconnectResultDTO() {}

    public static ReconnectResultDTO fail(String message) {
        ReconnectResultDTO res = new ReconnectResultDTO();
        res.setSuccess(false);
        res.setMessage(message);
        return res;
    }

    public static ReconnectResultDTO ok(GameStateDTO state) {
        ReconnectResultDTO res = new ReconnectResultDTO();
        res.setSuccess(true);
        res.setMessage("Reconnected successfully");
        res.setGameState(state);
        return res;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GameStateDTO getGameState() {
        return gameState;
    }

    public void setGameState(GameStateDTO gameState) {
        this.gameState = gameState;
    }
}
