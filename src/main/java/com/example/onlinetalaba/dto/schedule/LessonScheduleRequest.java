package com.example.onlinetalaba.dto.schedule;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonScheduleRequest {
    private String title;
    private String description;
    // Web va Flutter ikkalasi ham ISO-8601 formatda yuborishi kerak.
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean whiteboardEnabled;
    private boolean liveCommentsEnabled;
    private boolean liveVoiceQuestionsEnabled;
}