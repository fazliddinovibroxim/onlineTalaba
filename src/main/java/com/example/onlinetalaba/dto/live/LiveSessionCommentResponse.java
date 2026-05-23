package com.example.onlinetalaba.dto.live;

import com.example.onlinetalaba.enums.LessonCommentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LiveSessionCommentResponse {
    private Long id;
    private Long liveSessionId;
    private Long lessonScheduleId;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String senderUsername;
    private String content;
    private LessonCommentType commentType;
    private boolean edited;
    private LocalDateTime createdAt;
}
