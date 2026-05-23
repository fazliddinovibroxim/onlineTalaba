package com.example.onlinetalaba.dto.chatroom;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderName;
    private String senderUsername;
    private String message;
    private String attachmentUrl;
    private String attachmentName;
    private boolean isRead;
    private LocalDateTime createdAt;
}
