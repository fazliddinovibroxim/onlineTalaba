package com.example.onlinetalaba.dto.dashboard;

import com.example.onlinetalaba.enums.LiveSessionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DashboardLiveSessionResponse {
    private Long liveSessionId;
    private Long lessonId;
    private Long roomId;
    private String roomTitle;
    private String lessonTitle;
    private String hostName;
    private LiveSessionStatus status;
    private LocalDateTime startedAt;
}
