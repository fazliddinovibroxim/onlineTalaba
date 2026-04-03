package com.example.onlinetalaba.dto.publicview;

import com.example.onlinetalaba.enums.LibraryMaterialType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PublicMaterialMiniResponse {
    private Long materialId;
    private String title;
    private LibraryMaterialType materialType;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}
