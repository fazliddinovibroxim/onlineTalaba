package com.example.onlinetalaba.dto.chat;

import com.example.onlinetalaba.enums.ChatMessageType;
import lombok.Data;

@Data
public class RoomChatMessageRequest {
    private Long roomId;
    private String content;
    private ChatMessageType messageType;
}