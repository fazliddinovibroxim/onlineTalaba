package com.example.onlinetalaba.dto.stream;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StreamWhiteboardToggleCommand {
    private Long liveSessionId;
    private boolean enabled;
}

