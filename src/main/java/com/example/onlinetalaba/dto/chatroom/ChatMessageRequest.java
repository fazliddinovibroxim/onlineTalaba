package com.example.onlinetalaba.dto.chatroom;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private String message;
    private String attachmentUrl;
    private String attachmentName;
}
