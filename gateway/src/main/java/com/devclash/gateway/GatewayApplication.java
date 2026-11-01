package com.devclash.gateway;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        // Load .env variables into System properties if present
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        System.out.println("=================================================");
        System.out.println("   DEVCLASH - MULTIPLAYER TECH QUIZ BATTLE       ");
        System.out.println("   Spring Boot WebSocket & HTTP Gateway          ");
        System.out.println("=================================================");

        SpringApplication.run(GatewayApplication.class, args);
    }
}
