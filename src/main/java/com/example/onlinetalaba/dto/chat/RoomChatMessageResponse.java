package com.example.onlinetalaba.dto.chat;

import com.example.onlinetalaba.enums.ChatMessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomChatMessageResponse {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private String content;
    private ChatMessageType messageType;
    private boolean edited;
    private LocalDateTime createdAt;
}