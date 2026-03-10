package com.example.onlinetalaba.dto.auth;

import com.example.onlinetalaba.dto.dashboard.*;
import com.example.onlinetalaba.enums.AppPermissions;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.UserGender;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserDashboardResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String username;
    private AppRoleName role;
    private UserGender gender;
    private Set<AppPermissions> permissions;
    private DashboardStatsResponse stats;
    private List<DashboardRoomResponse> myRooms;
    private List<DashboardRoomResponse> discoverRooms;
    private List<DashboardLessonResponse> upcomingLessons;
    private List<DashboardLiveSessionResponse> liveSessions;
    private List<DashboardMaterialResponse> recentMaterials;
    private List<DashboardNotificationResponse> notifications;
    private List<DashboardJoinRequestResponse> pendingJoinRequests;
    private List<DashboardJoinRequestResponse> myJoinRequests;
    private DashboardSystemSummaryResponse systemSummary;
}
