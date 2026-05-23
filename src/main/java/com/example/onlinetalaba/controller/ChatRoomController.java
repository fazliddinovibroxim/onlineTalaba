package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.chatroom.*;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.ChatMessageService;
import com.example.onlinetalaba.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @PostMapping("/start")
    public ResponseEntity<ChatRoomResponse> start(
            @RequestBody ChatRoomStartRequest request,
            @CurrentUser User currentUser
    ) {
        Long otherUserId = request != null ? request.getOtherUserId() : null;
        return ResponseEntity.ok(chatRoomService.startOrGet(otherUserId, currentUser));
    }

    @PostMapping("/with/{otherUserId}")
    public ResponseEntity<ChatRoomResponse> startWithUser(
            @PathVariable Long otherUserId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(chatRoomService.startOrGet(otherUserId, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> list(@CurrentUser User currentUser) {
        return ResponseEntity.ok(chatRoomService.listMyRooms(currentUser));
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoomResponse> getOne(
            @PathVariable Long chatRoomId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(chatRoomService.getById(chatRoomId, currentUser));
    }

    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long chatRoomId,
            @CurrentUser User currentUser
    ) {
        chatRoomService.deleteRoom(chatRoomId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> listMessages(
            @PathVariable Long chatRoomId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(chatMessageService.list(chatRoomId, currentUser));
    }

    @GetMapping("/{chatRoomId}/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> getMessage(
            @PathVariable Long chatRoomId,
            @PathVariable Long messageId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(chatMessageService.getById(chatRoomId, messageId, currentUser));
    }

    @PostMapping("/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageResponse> createMessage(
            @PathVariable Long chatRoomId,
            @RequestBody ChatMessageRequest request,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(chatMessageService.create(chatRoomId, request, currentUser));
    }

    @PutMapping("/{chatRoomId}/messages/{messageId}")
    public ResponseEntity<ChatMessageResponse> updateMessage(
            @PathVariable Long chatRoomId,
            @PathVariable Long messageId,
            @RequestBody ChatMessageUpdateRequest request,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(chatMessageService.update(chatRoomId, messageId, request, currentUser));
    }

    @DeleteMapping("/{chatRoomId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long chatRoomId,
            @PathVariable Long messageId,
            @CurrentUser User currentUser
    ) {
        chatMessageService.delete(chatRoomId, messageId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{chatRoomId}/messages/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead(
            @PathVariable Long chatRoomId,
            @CurrentUser User currentUser
    ) {
        int updated = chatMessageService.markAllAsRead(chatRoomId, currentUser);
        return ResponseEntity.ok(Map.of("updatedCount", updated));
    }

    @PatchMapping("/{chatRoomId}/messages/{messageId}/read")
    public ResponseEntity<Void> markMessageRead(
            @PathVariable Long chatRoomId,
            @PathVariable Long messageId,
            @CurrentUser User currentUser
    ) {
        chatMessageService.markAsRead(chatRoomId, messageId, currentUser);
        return ResponseEntity.ok().build();
    }
}
