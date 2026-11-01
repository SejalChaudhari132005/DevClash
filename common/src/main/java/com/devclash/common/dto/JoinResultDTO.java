package com.devclash.common.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * Result returned to a player upon attempting to join a room.
 */
public class JoinResultDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String playerId;
    private String sessionToken;
    private RoomDTO room;

    public JoinResultDTO() {}

    public static JoinResultDTO fail(String message) {
        JoinResultDTO res = new JoinResultDTO();
        res.setSuccess(false);
        res.setMessage(message);
        return res;
    }

    public static JoinResultDTO ok(String playerId, String sessionToken, RoomDTO room) {
        JoinResultDTO res = new JoinResultDTO();
        res.setSuccess(true);
        res.setPlayerId(playerId);
        res.setSessionToken(sessionToken);
        res.setRoom(room);
        res.setMessage("Joined room successfully");
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

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public RoomDTO getRoom() {
        return room;
    }

    public void setRoom(RoomDTO room) {
        this.room = room;
    }
}
