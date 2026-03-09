package com.example.onlinetalaba.dto.library;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponse {
    private String serverName;
    private String originalName;
    private String contentType;
    private long size;
    private String fileUrl;
}