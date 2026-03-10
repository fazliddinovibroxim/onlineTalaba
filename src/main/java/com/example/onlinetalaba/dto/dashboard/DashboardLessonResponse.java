package com.example.onlinetalaba.dto.dashboard;

import com.example.onlinetalaba.enums.LessonStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DashboardLessonResponse {
    private Long lessonId;
    private Long roomId;
    private String roomTitle;
    private String title;
    private String teacherName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LessonStatus status;
}
