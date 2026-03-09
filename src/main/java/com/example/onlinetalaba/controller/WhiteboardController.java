package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.live.WhiteboardEventResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.WhiteboardEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/live-sessions/{liveSessionId}/whiteboard")
@RequiredArgsConstructor
public class WhiteboardController {

    private final WhiteboardEventService whiteboardEventService;

    @GetMapping("/history")
    public ResponseEntity<List<WhiteboardEventResponse>> history(
            @PathVariable Long liveSessionId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(whiteboardEventService.history(liveSessionId, currentUser));
    }
}