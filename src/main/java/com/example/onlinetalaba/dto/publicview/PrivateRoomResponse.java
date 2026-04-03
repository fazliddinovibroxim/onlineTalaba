package com.example.onlinetalaba.dto.publicview;

import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.enums.RoomVisibility;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PrivateRoomResponse {
    private Long roomId;
    private String title;
    private String subject;
    private String description;
    private RoomVisibility visibility;
    private boolean active;

    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    private long memberCount;
    private long teacherCount;
    private long activeLessonCount;
    private long weeklyLessonCount;
    private long resourceCount;
    private boolean liveNow;
    private long pendingJoinRequestCount;
    private long joinCount30d;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLessonAt;
    private LocalDateTime lastMaterialAt;
    private LocalDateTime lastActiveAt;
    private List<PublicLessonMiniResponse> upcomingLessons;
    private List<PublicMaterialMiniResponse> recentMaterials;

    private RoomMemberRole myRole;
    private boolean canManageRoom;
    private boolean canInviteMembers;
    private boolean canScheduleLesson;
    private boolean canUploadMaterials;
    private boolean myPendingJoinRequest;
}
