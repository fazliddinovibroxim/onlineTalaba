package com.example.onlinetalaba.dto.live;

import com.example.onlinetalaba.enums.LiveSessionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LiveSessionResponse {
    private Long id;
    private Long lessonScheduleId;
    private Long hostId;
    private String hostName;
    private String livekitRoomName;
    private LiveSessionStatus status;
    private boolean active;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}