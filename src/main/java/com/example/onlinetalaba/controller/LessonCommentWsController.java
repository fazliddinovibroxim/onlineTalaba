package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.config.StompPrincipal;
import com.example.onlinetalaba.dto.chat.LessonCommentRequest;
import com.example.onlinetalaba.dto.chat.LessonCommentResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.service.LessonCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class LessonCommentWsController {

    private final LessonCommentService lessonCommentService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/lessons.comments.send")
    public void sendLessonComment(@Payload LessonCommentRequest request, Principal principal) {
        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new RuntimeException("Unauthorized websocket user");
        }

        User currentUser = stompPrincipal.getUser();

        LessonCommentResponse response = lessonCommentService.send(request, currentUser);

        messagingTemplate.convertAndSend(
                "/topic/lessons/" + request.getLessonScheduleId() + "/comments",
                response
        );
    }
}