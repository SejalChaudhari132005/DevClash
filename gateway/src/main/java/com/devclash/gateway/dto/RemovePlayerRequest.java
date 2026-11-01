package com.devclash.gateway.dto;

public class RemovePlayerRequest {
    private String roomCode;
    private String hostPlayerId;
    private String targetPlayerId;

    public RemovePlayerRequest() {}

    public RemovePlayerRequest(String roomCode, String hostPlayerId, String targetPlayerId) {
        this.roomCode = roomCode;
        this.hostPlayerId = hostPlayerId;
        this.targetPlayerId = targetPlayerId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public void setHostPlayerId(String hostPlayerId) {
        this.hostPlayerId = hostPlayerId;
    }

    public String getTargetPlayerId() {
        return targetPlayerId;
    }

    public void setTargetPlayerId(String targetPlayerId) {
        this.targetPlayerId = targetPlayerId;
    }
}
