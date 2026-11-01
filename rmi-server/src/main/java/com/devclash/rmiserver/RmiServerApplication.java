package com.devclash.rmiserver;

import com.devclash.common.rmi.QuizRemote;
import com.devclash.rmiserver.config.DatabaseConfig;
import com.devclash.rmiserver.service.QuizRemoteImpl;
import io.github.cdimascio.dotenv.Dotenv;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Server Launcher for DevClash Java RMI Quiz Server.
 * Runs in its own standalone Java process and registers the remote object.
 */
public class RmiServerApplication {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   DEVCLASH - MULTIPLAYER TECH QUIZ BATTLE       ");
        System.out.println("   Standalone Java RMI Quiz Server Process       ");
        System.out.println("=================================================");

        Dotenv dotenv = loadDotenv();

        String host = getEnvString(dotenv, "RMI_HOST", "127.0.0.1");
        int port = getEnvInt(dotenv, "RMI_PORT", 1099);
        int objectPort = getEnvInt(dotenv, "RMI_OBJECT_PORT", 0);
        String serviceName = getEnvString(dotenv, "RMI_SERVICE_NAME", "DevClashQuizService");

        // Set RMI server hostname to prevent unresolved LAN/cloud connection refusal
        System.setProperty("java.rmi.server.hostname", host);

        // Initialize Database Connection Pool
        DatabaseConfig.initialize();

        try {
            // 1. Create or locate RMI Registry on the designated port
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(port);
                System.out.printf("[RMI Server] Created new RMI Registry on port %d%n", port);
            } catch (Exception e) {
                System.out.printf("[RMI Server] RMI Registry already active on port %d, connecting...%n", port);
                registry = LocateRegistry.getRegistry(port);
            }

            // 2. Instantiate Remote Object Implementation on objectPort (0 = dynamic, or fixed port e.g. 1098 for firewalls)
            QuizRemote quizService = new QuizRemoteImpl(objectPort);
            System.out.printf("[RMI Server] Exported QuizRemote remote object on port %s%n",
                    objectPort == 0 ? "dynamic (OS assigned)" : String.valueOf(objectPort));

            // 3. Bind the remote object using the registry name (e.g. DevClashQuizService)
            registry.rebind(serviceName, quizService);

            // Also rebind legacy alias if needed
            try {
                registry.rebind("QuizGameService", quizService);
            } catch (Exception ignored) {}

            System.out.println("-------------------------------------------------");
            System.out.printf("[RMI Server] Remote Object bound successfully!%n");
            System.out.printf("[RMI Server] Service Name: %s%n", serviceName);
            System.out.printf("[RMI Server] URL: rmi://localhost:%d/%s%n", port, serviceName);
            System.out.printf("[RMI Server] Registry Port: %d%n", port);
            System.out.println("[RMI Server] Ready to accept remote method calls from Gateway and test clients.");
            System.out.println("-------------------------------------------------");

            // Graceful shutdown hook
            final Registry finalRegistry = registry;
            final String finalServiceName = serviceName;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[RMI Server] Shutting down RMI Server...");
                try {
                    finalRegistry.unbind(finalServiceName);
                    System.out.println("[RMI Server] Unbound remote service: " + finalServiceName);
                } catch (Exception ignored) {}
                DatabaseConfig.close();
                System.out.println("[RMI Server] Shutdown complete.");
            }));

            // Keep process alive
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("[RMI Server] Critical failure starting RMI service: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Dotenv loadDotenv() {
        try {
            Dotenv d = Dotenv.configure().ignoreIfMissing().load();
            if (d.get("RMI_PORT") == null && d.get("DB_PASSWORD") == null) {
                return Dotenv.configure().directory("./rmi-server").ignoreIfMissing().load();
            }
            return d;
        } catch (Exception e) {
            return Dotenv.configure().ignoreIfMissing().load();
        }
    }

    private static int getEnvInt(Dotenv dotenv, String key, int defaultValue) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) {
            val = dotenv.get(key);
        }
        if (val != null && !val.isBlank()) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private static String getEnvString(Dotenv dotenv, String key, String defaultValue) {
        String val = System.getenv(key);
        if (val == null || val.isBlank()) {
            val = dotenv.get(key);
        }
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }
}
