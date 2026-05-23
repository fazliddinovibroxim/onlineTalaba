package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.live.LiveSessionCommentResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.LiveSessionCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/live-sessions/{liveSessionId}/comments")
@RequiredArgsConstructor
public class LiveSessionCommentController {

    private final LiveSessionCommentService liveSessionCommentService;

    @GetMapping
    public ResponseEntity<List<LiveSessionCommentResponse>> getComments(
            @PathVariable Long liveSessionId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(liveSessionCommentService.getComments(liveSessionId, currentUser));
    }
}
