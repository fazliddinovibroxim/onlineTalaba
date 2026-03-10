package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.publicview.PublicLiveLessonResponse;
import com.example.onlinetalaba.dto.publicview.PublicRoomResponse;
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
    public ResponseEntity<List<PublicRoomResponse>> getPublicRooms() {
        return ResponseEntity.ok(publicDiscoveryService.getPublicRooms());
    }

    @GetMapping("/public-live-lessons")
    public ResponseEntity<List<PublicLiveLessonResponse>> getPublicLiveLessons() {
        return ResponseEntity.ok(publicDiscoveryService.getPublicLiveLessons());
    }
}
