package com.example.onlinetalaba.dto.publicview;

import com.example.onlinetalaba.enums.LibraryMaterialType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PublicLibraryMaterialResponse {
    private Long id;
    private String title;
    private String description;
    private LibraryMaterialType materialType;
    private String fileUrl;
    private String originalName;
    private String contentType;
    private long size;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private PublicLibraryUploaderResponse uploader;
}
