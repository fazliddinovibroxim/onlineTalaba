package com.example.onlinetalaba.dto.schedule;

import com.example.onlinetalaba.enums.LessonStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LessonScheduleResponse {
    private Long id;
    private Long roomId;
    private Long teacherId;
    private String teacherName;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LessonStatus status;
    private boolean whiteboardEnabled;
    private boolean liveCommentsEnabled;
    private boolean liveVoiceQuestionsEnabled;
}
