// dto/notification/RoomNotificationMessage.java
package com.example.onlinetalaba.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomNotificationMessage {
    private String type;          // "LESSON_STARTED", "LESSON_ENDED" va h.k.
    private String title;
    private String body;
    private Long roomId;
    private Long liveSessionId;
    private Long lessonScheduleId;
    private LocalDateTime timestamp;
}