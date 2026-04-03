package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.publicview.PrivateRoomResponse;
import com.example.onlinetalaba.dto.publicview.PublicLiveLessonResponse;
import com.example.onlinetalaba.dto.publicview.PublicRoomRichResponse;
import com.example.onlinetalaba.dto.publicview.PublicSubjectSummaryResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.PublicCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final PublicCatalogService publicCatalogService;

    @GetMapping("/rooms/public")
    public ResponseEntity<List<PublicRoomRichResponse>> getPublicRooms(@CurrentUser User currentUser) {
        return ResponseEntity.ok(publicCatalogService.getPublicRooms(currentUser));
    }

    @GetMapping("/rooms/private")
    public ResponseEntity<List<PrivateRoomResponse>> getPrivateRooms(@CurrentUser User currentUser) {
        return ResponseEntity.ok(publicCatalogService.getPrivateRooms(currentUser));
    }

    @GetMapping("/rooms/public/{roomId}/preview")
    public ResponseEntity<PublicRoomRichResponse> getPublicRoomPreview(@PathVariable Long roomId,
                                                                       @CurrentUser User currentUser) {
        return ResponseEntity.ok(publicCatalogService.getPublicRoomPreview(roomId, currentUser));
    }

    @GetMapping("/live-lessons")
    public ResponseEntity<List<PublicLiveLessonResponse>> getPublicLiveLessons(@CurrentUser User currentUser) {
        return ResponseEntity.ok(publicCatalogService.getPublicLiveLessons(currentUser));
    }

    @GetMapping("/subjects/summary")
    public ResponseEntity<List<PublicSubjectSummaryResponse>> getPublicSubjectSummary(@CurrentUser User currentUser) {
        return ResponseEntity.ok(publicCatalogService.getSubjectSummary(currentUser));
    }
}
