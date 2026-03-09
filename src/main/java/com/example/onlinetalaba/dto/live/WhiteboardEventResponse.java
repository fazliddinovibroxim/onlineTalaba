package com.example.onlinetalaba.dto.live;

import com.example.onlinetalaba.enums.WhiteboardEventType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WhiteboardEventResponse {
    private Long id;
    private Long liveSessionId;
    private Long senderId;
    private String senderName;
    private WhiteboardEventType eventType;
    private String payloadJson;
    private LocalDateTime createdAt;
}