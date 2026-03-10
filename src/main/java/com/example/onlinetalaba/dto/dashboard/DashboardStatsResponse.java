package com.example.onlinetalaba.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long roomCount;
    private long ownedRoomCount;
    private long upcomingLessonCount;
    private long liveSessionCount;
    private long unreadNotificationCount;
    private long pendingJoinRequestCount;
    private long recentMaterialCount;
}
