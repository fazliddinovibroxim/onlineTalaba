package com.example.onlinetalaba.dto.library;

import com.example.onlinetalaba.enums.LibraryMaterialType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LibraryMaterialResponse {
    private Long id;
    private Long roomId;
    private Long uploadedById;
    private String uploadedByName;
    private String title;
    private String description;
    private LibraryMaterialType materialType;
    private boolean active;
    private AttachmentResponse attachment;
}