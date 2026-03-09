package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.room.RoomInviteRequest;
import com.example.onlinetalaba.dto.room.RoomRequest;
import com.example.onlinetalaba.dto.room.RoomResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponse> create(@RequestBody RoomRequest request,
                                               @CurrentUser User currentUser) {
        return ResponseEntity.ok(roomService.create(request, currentUser));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getById(roomId));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> update(@PathVariable Long roomId,
                                               @RequestBody RoomRequest request,
                                               @CurrentUser User currentUser) {
        return ResponseEntity.ok(roomService.update(roomId, request, currentUser));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> delete(@PathVariable Long roomId,
                                       @CurrentUser User currentUser) {
        roomService.delete(roomId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/invite")
    public ResponseEntity<Void> invite(@PathVariable Long roomId,
                                       @RequestBody RoomInviteRequest request,
                                       @CurrentUser User currentUser) {
        roomService.inviteMember(roomId, request, currentUser);
        return ResponseEntity.ok().build();
    }
}