package com.example.onlinetalaba.dto.room;

import com.example.onlinetalaba.enums.RoomMemberRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomMemberUserResponse {
    private Long userId;
    private String fullName;
    private String username;
    private String email;

    private RoomMemberRole roomRole;
    private boolean canManageRoom;
    private boolean canInviteMembers;
    private boolean canScheduleLesson;
    private boolean canUploadMaterials;
    private boolean active;
}

