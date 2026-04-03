package com.example.onlinetalaba.dto.publicview;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PublicLessonMiniResponse {
    private Long lessonId;
    private String title;
    private String teacherName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
