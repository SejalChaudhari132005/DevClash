package com.devclash.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Topic for room-wide broadcasts, queue/user for private user messages
        config.enableSimpleBroker("/topic", "/queue", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @org.springframework.beans.factory.annotation.Value("${devclash.cors.allowed-origins:${CORS_ALLOWED_ORIGINS:*}}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] patterns = java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (patterns.length == 0) {
            patterns = new String[]{"*"};
        }

        // Standard WebSocket endpoint
        registry.addEndpoint("/ws-devclash")
                .setAllowedOriginPatterns(patterns);

        // SockJS fallback for compatibility
        registry.addEndpoint("/ws-devclash")
                .setAllowedOriginPatterns(patterns)
                .withSockJS();
    }
}
