package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.config.StompPrincipal;
import com.example.onlinetalaba.dto.live.HandRaiseResponse;
import com.example.onlinetalaba.dto.stream.StreamHandRaiseRequest;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.handler.UnauthorizedException;
import com.example.onlinetalaba.service.LiveStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class StreamWebSocketController {

    private final LiveStreamService liveStreamService;
    private final SimpMessagingTemplate messagingTemplate;

    // /app/stream/{sessionId}/join
    @MessageMapping("/stream/{sessionId}/join")
    public void join(
            @DestinationVariable Long sessionId,
            @Payload(required = false) Object request,
            Principal principal
    ) {
        User currentUser = currentUser(principal);
        liveStreamService.join(sessionId, currentUser);
    }

    // /app/stream/{sessionId}/heartbeat
    @MessageMapping("/stream/{sessionId}/heartbeat")
    public void heartbeat(
            @DestinationVariable Long sessionId,
            @Payload(required = false) Object request,
            Principal principal
    ) {
        User currentUser = currentUser(principal);
        liveStreamService.heartbeat(sessionId, currentUser);
    }

    // /app/stream/{sessionId}/hand-raise
    @MessageMapping("/stream/{sessionId}/hand-raise")
    public void handRaise(
            @DestinationVariable Long sessionId,
            @Payload StreamHandRaiseRequest request,
            Principal principal
    ) {
        User currentUser = currentUser(principal);
        // Default to "raise" if payload is omitted by client.
        boolean raised = request == null || request.isRaised();
        HandRaiseResponse response = liveStreamService.handRaise(sessionId, raised, currentUser);

        // Teacher notification (existing channel). Participants list update is handled via LiveSessionService broadcast.
        if (response != null) {
            messagingTemplate.convertAndSend("/topic/live/" + sessionId + "/hand-raises", response);
        }
    }

    private User currentUser(Principal principal) {
        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new UnauthorizedException("Unauthorized websocket user");
        }
        return stompPrincipal.getUser();
    }
}
