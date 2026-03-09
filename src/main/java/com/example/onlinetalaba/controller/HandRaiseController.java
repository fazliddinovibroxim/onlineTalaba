package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.live.HandRaiseDecisionRequest;
import com.example.onlinetalaba.dto.live.HandRaiseResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.HandRaiseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/live-sessions/{liveSessionId}/hand-raises")
@RequiredArgsConstructor
public class HandRaiseController {

    private final HandRaiseService handRaiseService;

    @PostMapping
    public ResponseEntity<HandRaiseResponse> raise(
            @PathVariable Long liveSessionId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(handRaiseService.raiseHand(liveSessionId, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<HandRaiseResponse>> list(
            @PathVariable Long liveSessionId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(handRaiseService.list(liveSessionId, currentUser));
    }

    @PostMapping("/decision")
    public ResponseEntity<HandRaiseResponse> decide(
            @PathVariable Long liveSessionId,
            @RequestBody HandRaiseDecisionRequest request,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(handRaiseService.decide(request, currentUser));
    }
}