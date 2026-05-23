package com.example.onlinetalaba.dto.chatroom;

import com.example.onlinetalaba.dto.dashboard.UserSearchItemResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatRoomMeResponse {
    private Long chatRoomId;
    private UserSearchItemResponse otherUser;
    private String lastMessageText;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}
