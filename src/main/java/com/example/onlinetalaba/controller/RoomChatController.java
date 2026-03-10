package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.chat.RoomChatMessageResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.RoomChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/chat")
@RequiredArgsConstructor
public class RoomChatController {

    private final RoomChatService roomChatService;

    @PostMapping("/messages")
    public ResponseEntity<RoomChatMessageResponse> sendMessage(
            @PathVariable Long roomId,
            @RequestBody com.example.onlinetalaba.dto.chat.RoomChatMessageRequest request,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(roomChatService.sendToRoom(roomId, request, currentUser));
    }

    @PostMapping(value = "/images", consumes = {"multipart/form-data"})
    public ResponseEntity<RoomChatMessageResponse> sendImage(
            @PathVariable Long roomId,
            @RequestPart("file") MultipartFile file,
            @CurrentUser User currentUser
    ) throws java.io.IOException {
        return ResponseEntity.ok(roomChatService.sendImage(roomId, file, currentUser));
    }

    @PostMapping(value = "/files", consumes = {"multipart/form-data"})
    public ResponseEntity<RoomChatMessageResponse> sendFile(
            @PathVariable Long roomId,
            @RequestPart("file") MultipartFile file,
            @CurrentUser User currentUser
    ) throws java.io.IOException {
        return ResponseEntity.ok(roomChatService.sendFile(roomId, file, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<RoomChatMessageResponse>> getMessages(
            @PathVariable Long roomId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(roomChatService.getRoomMessages(roomId, currentUser));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long roomId,
            @PathVariable Long messageId,
            @CurrentUser User currentUser
    ) {
        roomChatService.deleteMessage(messageId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
