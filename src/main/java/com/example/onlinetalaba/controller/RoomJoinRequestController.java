package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.room.RoomJoinRequestDecisionRequest;
import com.example.onlinetalaba.dto.room.RoomJoinRequestRequest;
import com.example.onlinetalaba.dto.room.RoomJoinRequestResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.RoomJoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/join-requests")
@RequiredArgsConstructor
public class RoomJoinRequestController {

    private final RoomJoinRequestService roomJoinRequestService;

    @PostMapping
    public ResponseEntity<RoomJoinRequestResponse> create(
            @PathVariable Long roomId,
            @RequestBody(required = false) RoomJoinRequestRequest request,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(roomJoinRequestService.create(roomId, request, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<RoomJoinRequestResponse>> listPending(
            @PathVariable Long roomId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(roomJoinRequestService.getPendingRequests(roomId, currentUser));
    }

    @PostMapping("/{joinRequestId}/approve")
    public ResponseEntity<RoomJoinRequestResponse> approve(
            @PathVariable Long roomId,
            @PathVariable Long joinRequestId,
            @RequestBody(required = false) RoomJoinRequestDecisionRequest request,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(roomJoinRequestService.approve(roomId, joinRequestId, request, currentUser));
    }

    @PostMapping("/{joinRequestId}/reject")
    public ResponseEntity<RoomJoinRequestResponse> reject(
            @PathVariable Long roomId,
            @PathVariable Long joinRequestId,
            @RequestBody(required = false) RoomJoinRequestDecisionRequest request,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(roomJoinRequestService.reject(roomId, joinRequestId, request, currentUser));
    }
}
