package com.devclash.rmiserver.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Automatically initializes and verifies MySQL schema and initial question bank.
 * Ensures DevClash database has all tables, seeded categories, and 96 questions.
 */
public class DatabaseInitializer {

    public static void verifyAndMigrate() {
        if (!DatabaseConfig.isAvailable()) {
            System.out.println("[DatabaseInitializer] MySQL connection unavailable, skipping auto-migration.");
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // Check if categories table exists and has rows
            boolean needsSeeding = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    needsSeeding = true;
                }
            } catch (Exception e) {
                // Table does not exist
                needsSeeding = true;
            }

            // Also check questions count
            int questionCount = 0;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM questions")) {
                if (rs.next()) {
                    questionCount = rs.getInt(1);
                    if (questionCount == 0) {
                        needsSeeding = true;
                    }
                }
            } catch (Exception e) {
                needsSeeding = true;
            }

            if (needsSeeding) {
                System.out.println("[DatabaseInitializer] Database is empty or missing tables. Applying schema.sql...");
                applySchemaSql(conn);
            } else {
                System.out.printf("[DatabaseInitializer] Database verified! Found %d questions ready in MySQL.%n", questionCount);
            }

            // Print stats of historical matches
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM game_matches")) {
                if (rs.next()) {
                    System.out.printf("[DatabaseInitializer] Total archived matches in MySQL: %d%n", rs.getInt(1));
                }
            } catch (Exception ignored) {}

        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Error checking database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void applySchemaSql(Connection conn) {
        String schemaContent = null;

        // Try reading from relative and absolute paths
        String[] possiblePaths = {
            "database/schema.sql",
            "../database/schema.sql",
            "./schema.sql",
            "C:/Users/Sejal Pc/Desktop/DevClash/database/schema.sql"
        };

        for (String pStr : possiblePaths) {
            Path p = Paths.get(pStr);
            if (Files.exists(p)) {
                try {
                    schemaContent = Files.readString(p);
                    System.out.println("[DatabaseInitializer] Loaded schema from: " + p.toAbsolutePath());
                    break;
                } catch (Exception ignored) {}
            }
        }

        if (schemaContent == null) {
            System.err.println("[DatabaseInitializer] Could not locate database/schema.sql!");
            return;
        }

        List<String> statements = parseSqlStatements(schemaContent);
        int executed = 0;

        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (trimmed.isEmpty()) continue;
                String upper = trimmed.toUpperCase();
                if (upper.startsWith("CREATE DATABASE") || upper.startsWith("USE ")) {
                    continue;
                }
                try {
                    stmt.execute(trimmed);
                    executed++;
                } catch (Exception ex) {
                    System.err.println("[DatabaseInitializer] Statement warning: " + ex.getMessage());
                }
            }
            System.out.printf("[DatabaseInitializer] Successfully executed %d SQL statements from schema.sql.%n", executed);

            // Verify new counts
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM questions")) {
                if (rs.next()) {
                    System.out.printf("[DatabaseInitializer] Successfully seeded %d questions into MySQL!%n", rs.getInt(1));
                }
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
                if (rs.next()) {
                    System.out.printf("[DatabaseInitializer] Successfully seeded %d categories into MySQL!%n", rs.getInt(1));
                }
            }
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Failed to apply schema: " + e.getMessage());
        }
    }

    private static List<String> parseSqlStatements(String script) {
        List<String> list = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            char next = (i + 1 < script.length()) ? script.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n') inLineComment = false;
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }

            if (!inQuote) {
                if (c == '-' && next == '-') {
                    inLineComment = true;
                    i++;
                    continue;
                }
                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    i++;
                    continue;
                }
                if (c == '\'') {
                    inQuote = true;
                    current.append(c);
                    continue;
                }
                if (c == ';') {
                    String stmt = current.toString().trim();
                    if (!stmt.isEmpty()) {
                        list.add(stmt);
                    }
                    current.setLength(0);
                    continue;
                }
            } else {
                if (c == '\'') {
                    if (next == '\'') {
                        current.append(c).append(next);
                        i++;
                        continue;
                    } else {
                        inQuote = false;
                    }
                }
            }
            current.append(c);
        }
        if (!current.toString().trim().isEmpty()) {
            list.add(current.toString().trim());
        }
        return list;
    }

    public static void main(String[] args) {
        DatabaseConfig.initialize();
        verifyAndMigrate();
    }
}
