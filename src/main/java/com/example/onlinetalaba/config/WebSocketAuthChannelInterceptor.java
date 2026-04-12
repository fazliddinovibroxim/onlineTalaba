package com.example.onlinetalaba.config;

import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.repository.UserRepository;
import com.example.onlinetalaba.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

@Configuration
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = resolveAuthHeader(accessor);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing Authorization header");
            }

            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            User user = userRepository.findByEmail(email);
            if (user != null && jwtService.validateToken(token, user)) {
                accessor.setUser(new StompPrincipal(user));
            } else {
                throw new IllegalArgumentException("Invalid token");
            }
        }

        return message;
    }

    private String resolveAuthHeader(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null) {
            return authHeader;
        }

        authHeader = accessor.getFirstNativeHeader("authorization");
        if (authHeader != null) {
            return authHeader;
        }

        String token = accessor.getFirstNativeHeader("token");
        if (token == null || token.isBlank()) {
            return null;
        }

        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
