package com.example.onlinetalaba.dto.library;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponse {
    private UUID id;
    private String originalName;
    private String serverName;
    private String fileUrl;
    private String contentType;
    private long size;
}