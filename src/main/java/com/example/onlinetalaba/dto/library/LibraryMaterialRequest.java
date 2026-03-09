package com.example.onlinetalaba.dto.library;

import com.example.onlinetalaba.enums.LibraryMaterialType;
import lombok.Data;

@Data
public class LibraryMaterialRequest {
    private String title;
    private String description;
    private LibraryMaterialType materialType;
}