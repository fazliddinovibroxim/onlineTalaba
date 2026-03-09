package com.example.onlinetalaba.dto.chat;

import com.example.onlinetalaba.enums.LessonCommentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LessonCommentResponse {
    private Long id;
    private Long lessonScheduleId;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private String content;
    private LessonCommentType commentType;
    private boolean edited;
    private LocalDateTime createdAt;
}