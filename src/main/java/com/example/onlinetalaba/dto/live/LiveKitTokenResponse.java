package com.example.onlinetalaba.dto.live;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LiveKitTokenResponse {
    private String serverUrl;
    private String token;
    private String roomName;
    private Long liveSessionId;
    private boolean canPublish;
}
