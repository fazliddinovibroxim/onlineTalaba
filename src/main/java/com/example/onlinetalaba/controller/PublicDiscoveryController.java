package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.publicview.PublicLiveLessonResponse;
import com.example.onlinetalaba.dto.publicview.PrivateRoomResponse;
import com.example.onlinetalaba.dto.publicview.PublicRoomResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.PublicDiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discovery")
@RequiredArgsConstructor
public class PublicDiscoveryController {

    private final PublicDiscoveryService publicDiscoveryService;

    @GetMapping("/public-rooms")
    public ResponseEntity<List<PublicRoomResponse>> getPublicRooms(@CurrentUser User currentUser) {
        return ResponseEntity.ok(publicDiscoveryService.getPublicRooms(currentUser));
    }

    @GetMapping("/private-rooms")
    public ResponseEntity<List<PrivateRoomResponse>> getPrivateRooms(@CurrentUser User currentUser) {
        return ResponseEntity.ok(publicDiscoveryService.getPrivateRooms(currentUser));
    }

    @GetMapping("/public-live-lessons")
    public ResponseEntity<List<PublicLiveLessonResponse>> getPublicLiveLessons(@CurrentUser User currentUser) {
        return ResponseEntity.ok(publicDiscoveryService.getPublicLiveLessons(currentUser));
    }
}
