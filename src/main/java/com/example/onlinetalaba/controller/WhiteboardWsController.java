package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.config.StompPrincipal;
import com.example.onlinetalaba.dto.live.WhiteboardEventRequest;
import com.example.onlinetalaba.dto.live.WhiteboardEventResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.handler.UnauthorizedException;
import com.example.onlinetalaba.service.WhiteboardEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WhiteboardWsController {

    private final WhiteboardEventService whiteboardEventService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/live.whiteboard.event")
    public void sendEvent(@Payload WhiteboardEventRequest request, Principal principal) {
        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new UnauthorizedException("Unauthorized websocket user");
        }

        User currentUser = stompPrincipal.getUser();

        WhiteboardEventResponse response = whiteboardEventService.saveEvent(request, currentUser);

        messagingTemplate.convertAndSend(
                "/topic/live/" + request.getLiveSessionId() + "/whiteboard",
                response
        );
    }
}
