package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.config.StompPrincipal;
import com.example.onlinetalaba.dto.chat.RoomChatMessageRequest;
import com.example.onlinetalaba.dto.chat.RoomChatMessageResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.service.RoomChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class RoomChatWsController {

    private final RoomChatService roomChatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/rooms.chat.send")
    public void sendRoomChat(@Payload RoomChatMessageRequest request, Principal principal) {
        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new RuntimeException("Unauthorized websocket user");
        }

        User currentUser = stompPrincipal.getUser();

        RoomChatMessageResponse response = roomChatService.send(request, currentUser);

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + request.getRoomId() + "/chat",
                response
        );
    }
}