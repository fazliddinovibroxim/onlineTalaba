package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.config.StompPrincipal;
import com.example.onlinetalaba.dto.live.LiveSessionCommentRequest;
import com.example.onlinetalaba.dto.live.LiveSessionCommentResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.handler.UnauthorizedException;
import com.example.onlinetalaba.service.LiveSessionCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class LiveSessionCommentWsController {

    private final LiveSessionCommentService liveSessionCommentService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/stream/{sessionId}/comments/send")
    public void sendComment(
            @DestinationVariable Long sessionId,
            @Payload LiveSessionCommentRequest request,
            Principal principal
    ) {
        User currentUser = currentUser(principal);
        if (request == null) {
            request = new LiveSessionCommentRequest();
        }
        request.setLiveSessionId(sessionId);

        LiveSessionCommentResponse response = liveSessionCommentService.send(sessionId, request, currentUser);

        messagingTemplate.convertAndSend(
                "/topic/stream/" + sessionId + "/comments",
                response
        );
    }

    private User currentUser(Principal principal) {
        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new UnauthorizedException("Unauthorized websocket user");
        }
        return stompPrincipal.getUser();
    }
}
