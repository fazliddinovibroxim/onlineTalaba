package com.example.onlinetalaba.dto.live;

import com.example.onlinetalaba.enums.HandRaiseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HandRaiseResponse {
    private Long id;
    private Long liveSessionId;
    private Long userId;
    private String userName;
    private HandRaiseStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
}