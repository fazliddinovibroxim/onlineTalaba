package com.example.onlinetalaba.dto.live;

import com.example.onlinetalaba.enums.LessonCommentType;
import lombok.Data;

@Data
public class LiveSessionCommentRequest {
    private Long liveSessionId;
    private String content;
    private LessonCommentType commentType;
}
