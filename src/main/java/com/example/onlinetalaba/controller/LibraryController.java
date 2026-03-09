package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.library.LibraryMaterialRequest;
import com.example.onlinetalaba.dto.library.LibraryMaterialResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<LibraryMaterialResponse> upload(
            @PathVariable Long roomId,
            @RequestPart("data") LibraryMaterialRequest request,
            @RequestPart("file") MultipartFile file,
            @CurrentUser User currentUser
    ) throws IOException {
        return ResponseEntity.ok(libraryService.uploadToRoom(roomId, request, file, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<LibraryMaterialResponse>> getAll(
            @PathVariable Long roomId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(libraryService.getAllByRoom(roomId, currentUser));
    }

    @GetMapping("/{materialId}")
    public ResponseEntity<LibraryMaterialResponse> getById(
            @PathVariable Long roomId,
            @PathVariable Long materialId,
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(libraryService.getById(roomId, materialId, currentUser));
    }

    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long roomId,
            @PathVariable Long materialId,
            @CurrentUser User currentUser
    ) throws IOException {
        libraryService.deleteMaterial(roomId, materialId, currentUser);
        return ResponseEntity.noContent().build();
    }
}