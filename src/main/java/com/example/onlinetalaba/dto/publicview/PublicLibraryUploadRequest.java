package com.example.onlinetalaba.dto.publicview;

import com.example.onlinetalaba.enums.LibraryMaterialType;
import lombok.Data;

@Data
public class PublicLibraryUploadRequest {
    private String title;
    private String description;
    private LibraryMaterialType materialType;
}
