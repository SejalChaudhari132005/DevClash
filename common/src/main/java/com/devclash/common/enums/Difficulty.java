package com.devclash.common.enums;

import java.io.Serializable;

/**
 * Quiz question difficulty levels.
 */
public enum Difficulty implements Serializable {
    EASY("Easy", 1.0),
    MEDIUM("Medium", 1.5),
    HARD("Hard", 2.0);

    private final String label;
    private final double multiplier;

    Difficulty(String label, double multiplier) {
        this.label = label;
        this.multiplier = multiplier;
    }

    public String getLabel() {
        return label;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
