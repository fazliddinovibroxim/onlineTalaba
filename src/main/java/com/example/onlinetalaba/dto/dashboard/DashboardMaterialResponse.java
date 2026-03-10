package com.example.onlinetalaba.dto.dashboard;

import com.example.onlinetalaba.enums.LibraryMaterialType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DashboardMaterialResponse {
    private Long materialId;
    private Long roomId;
    private String roomTitle;
    private String title;
    private String uploadedBy;
    private LibraryMaterialType materialType;
    private String fileUrl;
    private LocalDateTime createdAt;
}
