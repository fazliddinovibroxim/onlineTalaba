package com.example.onlinetalaba.dto.room;

import com.example.onlinetalaba.enums.RoomMemberRole;
import lombok.Data;

@Data
public class RoomInviteRequest {
    private Long userId;
    private RoomMemberRole role;
    private boolean canManageRoom;
    private boolean canInviteMembers;
    private boolean canScheduleLesson;
    private boolean canUploadMaterials;
}