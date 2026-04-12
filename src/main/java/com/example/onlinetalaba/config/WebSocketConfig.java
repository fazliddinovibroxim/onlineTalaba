package com.example.onlinetalaba.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:5173",
                        "http://localhost:8080",
                        "http://localhost:53920",
                        "https://talaba.pulhisob.uz",
                        "https://www.talaba.pulhisob.uz"
                )
                .withSockJS();

 //       flutter uchun sockJS siz path
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns(
                        "http://localhost:*",
                        "https://talaba.pulhisob.uz"
                );
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor);
    }
}