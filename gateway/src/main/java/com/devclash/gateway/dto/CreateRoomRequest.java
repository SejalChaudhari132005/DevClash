package com.devclash.gateway.dto;

import com.devclash.common.dto.GameConfigDTO;

public class CreateRoomRequest {
    private String nickname;
    private String avatar;
    private GameConfigDTO config;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public GameConfigDTO getConfig() {
        return config;
    }

    public void setConfig(GameConfigDTO config) {
        this.config = config;
    }
}
