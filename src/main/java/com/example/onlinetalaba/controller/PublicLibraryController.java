package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.publicview.PublicLibraryMaterialResponse;
import com.example.onlinetalaba.dto.publicview.PublicLibraryUploadRequest;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.LibraryMaterialType;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.PublicLibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public/library")
@RequiredArgsConstructor
public class PublicLibraryController {

    private final PublicLibraryService publicLibraryService;

    @PostMapping(value = "/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PublicLibraryMaterialResponse> upload(
            @RequestPart("data") PublicLibraryUploadRequest request,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            // Backward-compat: some clients send a single part named "file"
            @RequestPart(value = "file", required = false) MultipartFile file,
            @CurrentUser User currentUser
    ) throws IOException {
        return ResponseEntity.ok(publicLibraryService.upload(request, files, file, currentUser));
    }

    @GetMapping("/materials")
    public ResponseEntity<List<PublicLibraryMaterialResponse>> getAll(
            @RequestParam(required = false) LibraryMaterialType materialType
    ) {
        return ResponseEntity.ok(publicLibraryService.getAll(materialType));
    }

    @GetMapping("/materials/my")
    public ResponseEntity<List<PublicLibraryMaterialResponse>> getMine(@CurrentUser User currentUser) {
        return ResponseEntity.ok(publicLibraryService.getMyMaterials(currentUser));
    }

    @GetMapping("/materials/{materialId}")
    public ResponseEntity<PublicLibraryMaterialResponse> getById(@PathVariable Long materialId) {
        return ResponseEntity.ok(publicLibraryService.getById(materialId));
    }

    @DeleteMapping("/materials/{materialId}")
    public ResponseEntity<Void> delete(@PathVariable Long materialId,
                                       @CurrentUser User currentUser) throws IOException {
        publicLibraryService.delete(materialId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
