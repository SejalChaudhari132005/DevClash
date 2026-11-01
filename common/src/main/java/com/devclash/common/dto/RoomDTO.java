package com.devclash.common.dto;

import com.devclash.common.enums.RoomState;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a DevClash game room.
 */
public class RoomDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roomCode;
    private String hostPlayerId;
    private String hostNickname;
    private GameConfigDTO config;
    private RoomState state;
    private List<PlayerDTO> players = new ArrayList<>();
    private String hostSessionToken;

    public RoomDTO() {}

    public RoomDTO(String roomCode, String hostPlayerId, String hostNickname, GameConfigDTO config) {
        this.roomCode = roomCode;
        this.hostPlayerId = hostPlayerId;
        this.hostNickname = hostNickname;
        this.config = config;
        this.state = RoomState.WAITING_LOBBY;
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

    public String getHostNickname() {
        return hostNickname;
    }

    public void setHostNickname(String hostNickname) {
        this.hostNickname = hostNickname;
    }

    public GameConfigDTO getConfig() {
        return config;
    }

    public void setConfig(GameConfigDTO config) {
        this.config = config;
    }

    public RoomState getState() {
        return state;
    }

    public void setState(RoomState state) {
        this.state = state;
    }

    public List<PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDTO> players) {
        this.players = players;
    }

    public String getHostSessionToken() {
        return hostSessionToken;
    }

    public void setHostSessionToken(String hostSessionToken) {
        this.hostSessionToken = hostSessionToken;
    }
}
