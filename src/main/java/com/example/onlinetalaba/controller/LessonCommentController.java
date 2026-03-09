package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.chat.LessonCommentResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.LessonCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons/{lessonScheduleId}/comments")
@RequiredArgsConstructor
public class LessonCommentController {

    private final LessonCommentService lessonCommentService;

    @GetMapping
    public ResponseEntity<List<LessonCommentResponse>> getComments(
            @PathVariable Long lessonScheduleId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(lessonCommentService.getLessonComments(lessonScheduleId, currentUser));
    }
}