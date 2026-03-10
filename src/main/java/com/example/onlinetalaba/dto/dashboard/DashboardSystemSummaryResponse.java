package com.example.onlinetalaba.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSystemSummaryResponse {
    private long totalUsers;
    private long totalRooms;
    private long totalPrivateRooms;
    private long totalPublicRooms;
    private long totalLiveSessions;
    private long totalScheduledLessons;
    private long totalPendingJoinRequests;
}
