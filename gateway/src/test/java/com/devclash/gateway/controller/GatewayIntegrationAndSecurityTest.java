package com.devclash.gateway.controller;

import com.devclash.common.dto.*;
import com.devclash.common.enums.Difficulty;
import com.devclash.common.enums.GameMode;
import com.devclash.common.rmi.QuizRemote;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.rmi.RemoteException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {QuizGatewayRestController.class, HealthRestController.class})
public class GatewayIntegrationAndSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizRemote quizRemote;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Health endpoint returns UP when RMI is reachable")
    void testHealthEndpointRmiAvailable() throws Exception {
        when(quizRemote.ping()).thenReturn(true);

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gatewayStatus").value("UP"))
                .andExpect(jsonPath("$.rmiServerStatus").value("UP"));
    }

    @Test
    @DisplayName("Health endpoint returns DOWN when RMI server is unavailable")
    void testHealthEndpointRmiUnavailable() throws Exception {
        when(quizRemote.ping()).thenThrow(new RemoteException("RMI Registry unreachable at localhost:1099"));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gatewayStatus").value("UP"))
                .andExpect(jsonPath("$.rmiServerStatus").value("DOWN"));
    }

    @Test
    @DisplayName("Create room forwards request to RMI server and returns RoomDTO with Host ID")
    void testCreateRoomSuccess() throws Exception {
        GameConfigDTO config = new GameConfigDTO(1, "PROGRAMMING", Difficulty.MEDIUM, GameMode.CLASSIC);
        RoomDTO room = new RoomDTO("TEST01", "p_host", "Alice", config);

        when(quizRemote.createRoom(eq("Alice"), anyString(), any(GameConfigDTO.class))).thenReturn(room);

        String jsonPayload = """
                {
                    "nickname": "Alice",
                    "avatar": "terminal",
                    "config": {
                        "gameMode": "CLASSIC",
                        "difficulty": "MEDIUM",
                        "totalQuestions": 5,
                        "questionTimeLimitSec": 20
                    }
                }
                """;

        mockMvc.perform(post("/api/quiz/room/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomCode").value("TEST01"))
                .andExpect(jsonPath("$.hostPlayerId").value("p_host"));
    }

    @Test
    @DisplayName("Join room failure: Duplicate nickname is rejected by RMI and returned by Gateway")
    void testJoinRoomDuplicateNickname() throws Exception {
        when(quizRemote.joinRoom(eq("TEST01"), eq("Alice"), anyString()))
                .thenReturn(JoinResultDTO.fail("Nickname 'Alice' is already taken in this room."));

        String jsonPayload = """
                {
                    "roomCode": "TEST01",
                    "nickname": "Alice",
                    "avatar": "terminal"
                }
                """;

        mockMvc.perform(post("/api/quiz/room/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Nickname 'Alice' is already taken in this room."));
    }

    @Test
    @DisplayName("Join room failure: Invalid or non-existent room code is rejected")
    void testJoinRoomInvalidCode() throws Exception {
        when(quizRemote.joinRoom(eq("INVALID"), eq("Bob"), anyString()))
                .thenReturn(JoinResultDTO.fail("Room 'INVALID' not found."));

        String jsonPayload = """
                {
                    "roomCode": "INVALID",
                    "nickname": "Bob",
                    "avatar": "wizard"
                }
                """;

        mockMvc.perform(post("/api/quiz/room/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Room 'INVALID' not found."));
    }

    @Test
    @DisplayName("Security: Non-host attempting host-only action (kick player) is rejected with error")
    void testHostAuthorizationKickAttempt() throws Exception {
        when(quizRemote.removePlayer(eq("TEST01"), eq("p_fake_host"), eq("p_victim")))
                .thenThrow(new SecurityException("Only the room host can remove players."));

        String jsonPayload = """
                {
                    "roomCode": "TEST01",
                    "hostPlayerId": "p_fake_host",
                    "targetPlayerId": "p_victim"
                }
                """;

        mockMvc.perform(post("/api/quiz/room/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only the room host can remove players."));
    }

    @Test
    @DisplayName("Answer submission: Client cannot forge points - Gateway delegates evaluation strictly to RMI")
    void testAnswerSubmissionScoringAuthority() throws Exception {
        AnswerResultDTO result = AnswerResultDTO.evaluated(true, 850, 850, 1, 1200L);
        when(quizRemote.submitAnswer(eq("TEST01"), eq("p_bob"), eq(1), eq("C"), eq(1200L)))
                .thenReturn(result);

        String jsonPayload = """
                {
                    "roomCode": "TEST01",
                    "playerId": "p_bob",
                    "questionIndex": 1,
                    "selectedOption": "C",
                    "responseTimeMs": 1200,
                    "forgedScore": 99999,
                    "isCorrect": true
                }
                """;

        mockMvc.perform(post("/api/quiz/room/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true))
                .andExpect(jsonPath("$.pointsAwarded").value(850));

        // Verify RMI invocation received only authoritative fields
        verify(quizRemote, times(1)).submitAnswer(eq("TEST01"), eq("p_bob"), eq(1), eq("C"), eq(1200L));
    }

    @Test
    @DisplayName("RMI Failure Resiliency: Gateway returns structured 500 error without crash when RMI fails")
    void testRmiServerFailureGracefulHandling() throws Exception {
        when(quizRemote.getRoomState(eq("TEST01"), isNull()))
                .thenThrow(new RemoteException("Connection refused to host: 127.0.0.1; nested exception is: java.net.ConnectException"));

        mockMvc.perform(get("/api/quiz/room/TEST01/state"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Player Reconnect: Returns refreshed room and player state")
    void testPlayerReconnect() throws Exception {
        GameConfigDTO config = new GameConfigDTO(1, "PROGRAMMING", Difficulty.MEDIUM, GameMode.CLASSIC);
        RoomDTO room = new RoomDTO("TEST01", "p_host", "Alice", config);

        when(quizRemote.setPlayerConnected(eq("TEST01"), eq("p_player2"), eq(true))).thenReturn(true);
        when(quizRemote.getRoom(eq("TEST01"))).thenReturn(room);

        String jsonPayload = """
                {
                    "roomCode": "TEST01",
                    "playerId": "p_player2"
                }
                """;

        mockMvc.perform(post("/api/quiz/room/reconnect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.room.roomCode").value("TEST01"));

        verify(quizRemote).setPlayerConnected("TEST01", "p_player2", true);
    }
}
