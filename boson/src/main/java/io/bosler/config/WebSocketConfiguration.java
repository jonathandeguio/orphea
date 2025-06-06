package io.bosler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {


    @Value("${app.cors.allowedOrigins}")
    private String[] allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/api/ws")

                .setAllowedOriginPatterns(allowedOrigins)

                .withSockJS()
                .setWebSocketEnabled(true)
                .setHeartbeatTime(10000)
                .setDisconnectDelay(10000)
                .setClientLibraryUrl("/webjars/sockjs-client/1.5.1/sockjs.min.js")
                .setSessionCookieNeeded(false);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/project", "/queue");
        registry.setApplicationDestinationPrefixes("/app");

    }
}