package com.example.onlinetalaba.dto.publicview;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PublicLiveLessonResponse {
    private Long liveSessionId;
    private Long lessonScheduleId;
    private String lessonTitle;
    private String lessonDescription;
    private String teacherName;
    private LocalDateTime startedAt;
    private LocalDateTime endTime;
    private long participantCount;
    private PublicLiveRoomSummaryResponse room;
}
