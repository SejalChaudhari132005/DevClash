package com.devclash.gateway.dto;

public class LeaveRoomRequest {
    private String roomCode;
    private String playerId;

    public LeaveRoomRequest() {}

    public LeaveRoomRequest(String roomCode, String playerId) {
        this.roomCode = roomCode;
        this.playerId = playerId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
