package com.example.onlinetalaba.dto.chatroom;

import lombok.Data;

@Data
public class ChatMessageUpdateRequest {
    private String message;
    private String attachmentUrl;
    private String attachmentName;
}
