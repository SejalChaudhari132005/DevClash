package com.devclash.rmiserver.integration;

import com.devclash.rmiserver.config.DatabaseConfig;
import com.devclash.rmiserver.config.DatabaseInitializer;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseMigrationTest {

    @Test
    public void testDatabaseMigrationAndVerification() throws Exception {
        DatabaseConfig.initialize();
        if (!DatabaseConfig.isAvailable()) {
            System.out.println("MySQL not available on this machine, skipping live migration assertion.");
            return;
        }

        DatabaseInitializer.verifyAndMigrate();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // Assert categories populated
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
                assertTrue(rs.next());
                int catCount = rs.getInt(1);
                System.out.println("VERIFIED: Categories count in MySQL = " + catCount);
                assertTrue(catCount >= 6, "Expected at least 6 categories in MySQL");
            }

            // Assert questions populated
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM questions")) {
                assertTrue(rs.next());
                int qCount = rs.getInt(1);
                System.out.println("VERIFIED: Questions count in MySQL = " + qCount);
                assertTrue(qCount >= 90, "Expected at least 90 questions in MySQL");
            }

            // Assert game_matches table exists and inspect latest
            try (ResultSet rs = stmt.executeQuery("SELECT id, room_code, winner_nickname, winner_score, total_players, ended_at FROM game_matches ORDER BY id DESC LIMIT 5")) {
                System.out.println("--- LATEST 5 GAME MATCHES IN MYSQL ---");
                while (rs.next()) {
                    System.out.printf("Match #%d: Room %s | Winner: %s (%d PTS) | Players: %d | Time: %s%n",
                            rs.getLong("id"), rs.getString("room_code"), rs.getString("winner_nickname"),
                            rs.getInt("winner_score"), rs.getInt("total_players"), rs.getString("ended_at"));
                }
            }

            // Assert match_players table exists
            try (ResultSet rs = stmt.executeQuery("SELECT id, match_id, nickname, final_score, rank_position FROM match_players ORDER BY id DESC LIMIT 5")) {
                System.out.println("--- LATEST 5 MATCH PLAYERS IN MYSQL ---");
                while (rs.next()) {
                    System.out.printf("Player #%d: Match %d | %s (%d PTS) | Rank #%d%n",
                            rs.getLong("id"), rs.getLong("match_id"), rs.getString("nickname"),
                            rs.getInt("final_score"), rs.getInt("rank_position"));
                }
            }

            // Check match_answers table
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM match_answers")) {
                if (rs.next()) {
                    System.out.println("match_answers count in MySQL = " + rs.getInt(1));
                }
            }
        }
    }
}
