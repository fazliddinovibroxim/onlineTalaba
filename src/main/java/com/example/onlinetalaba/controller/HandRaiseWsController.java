package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.config.StompPrincipal;
import com.example.onlinetalaba.dto.live.HandRaiseDecisionRequest;
import com.example.onlinetalaba.dto.live.HandRaiseResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.service.HandRaiseService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HandRaiseWsController {

    private final HandRaiseService handRaiseService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/live.handraise.raise")
    public void raise(@Payload Long liveSessionId, Principal principal) {
        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new RuntimeException("Unauthorized websocket user");
        }

        User currentUser = stompPrincipal.getUser();
        HandRaiseResponse response = handRaiseService.raiseHand(liveSessionId, currentUser);

        messagingTemplate.convertAndSend(
                "/topic/live/" + liveSessionId + "/hand-raises",
                response
        );
    }

    @MessageMapping("/live.handraise.decision")
    public void decide(@Payload HandRaiseDecisionRequest request, Principal principal) {
        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new RuntimeException("Unauthorized websocket user");
        }

        User currentUser = stompPrincipal.getUser();
        HandRaiseResponse response = handRaiseService.decide(request, currentUser);

        messagingTemplate.convertAndSend(
                "/topic/live/" + response.getLiveSessionId() + "/hand-raises",
                response
        );
    }
}