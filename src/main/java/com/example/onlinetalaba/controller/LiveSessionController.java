package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.live.LiveKitTokenResponse;
import com.example.onlinetalaba.dto.live.LiveSessionResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.LiveSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/live-sessions")
@RequiredArgsConstructor
public class LiveSessionController {

    private final LiveSessionService liveSessionService;

    @PostMapping("/lesson/{lessonScheduleId}/start")
    public ResponseEntity<LiveSessionResponse> start(
            @PathVariable Long lessonScheduleId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(liveSessionService.createOrStart(lessonScheduleId, currentUser));
    }

    @GetMapping("/lesson/{lessonScheduleId}")
    public ResponseEntity<LiveSessionResponse> getByLesson(
            @PathVariable Long lessonScheduleId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(liveSessionService.getByLessonSchedule(lessonScheduleId, currentUser));
    }

    @PostMapping("/{liveSessionId}/token")
    public ResponseEntity<LiveKitTokenResponse> issueToken(
            @PathVariable Long liveSessionId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(liveSessionService.issueToken(liveSessionId, currentUser));
    }

    @PostMapping("/{liveSessionId}/end")
    public ResponseEntity<Void> end(
            @PathVariable Long liveSessionId,
            @CurrentUser User currentUser
    ) {
        liveSessionService.end(liveSessionId, currentUser);
        return ResponseEntity.ok().build();
    }
}