package com.devclash.common.enums;

import java.io.Serializable;

/**
 * Supported multiplayer game modes in DevClash.
 */
public enum GameMode implements Serializable {
    CLASSIC("Classic Battle", "Standard point scoring with streak bonuses for consecutive correct answers."),
    SPEED_RUSH("Speed Rush", "Rapid score decay based on reaction speed. Fast answers win the match!"),
    BRAIN_BATTLE("Brain Battle", "High stakes: +1200 for correct answers, -300 point penalty for wrong answers.");

    private final String displayName;
    private final String description;

    GameMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
