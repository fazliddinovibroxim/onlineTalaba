package com.example.onlinetalaba.dto.chat;

import com.example.onlinetalaba.enums.LessonCommentType;
import lombok.Data;

@Data
public class LessonCommentRequest {
    private Long lessonScheduleId;
    private String content;
    private LessonCommentType commentType;
}