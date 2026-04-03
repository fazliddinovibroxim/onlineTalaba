package com.example.onlinetalaba.dto.publicview;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicSubjectSummaryResponse {
    private String subject;
    private long roomCount;
    private long liveRoomCount;
    private long weeklyLessonCount;
    private long activeMemberCount;
}
