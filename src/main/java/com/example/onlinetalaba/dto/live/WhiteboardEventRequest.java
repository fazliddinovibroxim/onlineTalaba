package com.example.onlinetalaba.dto.live;

import com.example.onlinetalaba.enums.WhiteboardEventType;
import lombok.Data;

@Data
public class WhiteboardEventRequest {
    private Long liveSessionId;
    private WhiteboardEventType eventType;
    private String payloadJson;
}