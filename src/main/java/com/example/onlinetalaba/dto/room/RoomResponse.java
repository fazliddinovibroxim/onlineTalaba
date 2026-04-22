package com.example.onlinetalaba.dto.room;

import com.example.onlinetalaba.enums.RoomVisibility;
import com.example.onlinetalaba.enums.RoomMemberRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoomResponse {
    private Long id;
    private String title;
    private String description;
    private String subject;
    private RoomVisibility visibility;
    private Long ownerId;
    private String ownerName;
    private boolean active;
    private long memberCount;
    private boolean liveNow;
    private long pendingJoinRequestCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean member;
    private RoomMemberRole myRole;
    private boolean canManageRoom;
    private boolean canInviteMembers;
    private boolean canScheduleLesson;
    private boolean canUploadMaterials;
    private boolean myPendingJoinRequest;

    // Optional: single-room endpointlarda qaytadi (search/listda bo'sh bo'lishi mumkin)
    private List<RoomMemberUserResponse> members;
}
