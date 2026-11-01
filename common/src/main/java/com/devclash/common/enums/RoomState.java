package com.devclash.common.enums;

import java.io.Serializable;

/**
 * Robust State Machine states for a DevClash Game Room:
 * WAITING → COUNTDOWN → QUESTION_ACTIVE → QUESTION_RESULT → NEXT_QUESTION → FINISHED.
 */
public enum RoomState implements Serializable {
    WAITING,
    COUNTDOWN,
    QUESTION_ACTIVE,
    QUESTION_RESULT,
    NEXT_QUESTION,
    FINISHED,
    CLOSED;

    // Aliases for backwards compatibility with existing codebase
    public static final RoomState WAITING_LOBBY = WAITING;
    public static final RoomState STARTING_COUNTDOWN = COUNTDOWN;
    public static final RoomState QUESTION_SUMMARY = QUESTION_RESULT;
    public static final RoomState MATCH_FINISHED = FINISHED;
}
