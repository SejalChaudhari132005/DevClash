package com.devclash.rmiserver.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {

    private static HikariDataSource dataSource;

    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        Dotenv dotenv;
        try {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
            if (dotenv.get("DB_PASSWORD") == null) {
                dotenv = Dotenv.configure().directory("./rmi-server").ignoreIfMissing().load();
            }
        } catch (Exception e) {
            dotenv = Dotenv.configure().ignoreIfMissing().load();
        }

        String host = getEnv(dotenv, "DB_HOST", "localhost");
        String port = getEnv(dotenv, "DB_PORT", "3306");
        String dbName = getEnv(dotenv, "DB_NAME", "devclash_db");
        String user = getEnv(dotenv, "DB_USER", "root");
        String password = getEnv(dotenv, "DB_PASSWORD", "");

        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                host, port, dbName);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool configuration
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setPoolName("DevClash-HikariCP");

        try {
            dataSource = new HikariDataSource(config);
            try (Connection conn = dataSource.getConnection()) {
                System.out.println("[DatabaseConfig] Successfully connected to MySQL at: " + jdbcUrl);
            }
        } catch (Exception e) {
            System.err.println("[DatabaseConfig] Warning: MySQL connection failed (" + e.getMessage() + "). Question bank will use embedded fallback if needed.");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource.getConnection();
    }

    public static boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }

    private static String getEnv(Dotenv dotenv, String key, String defaultValue) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) {
            val = dotenv.get(key);
        }
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DatabaseConfig] Connection pool closed.");
        }
    }
}
