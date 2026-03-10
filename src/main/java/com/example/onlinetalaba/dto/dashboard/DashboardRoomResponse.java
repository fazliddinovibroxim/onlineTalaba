package com.example.onlinetalaba.dto.dashboard;

import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.enums.RoomVisibility;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardRoomResponse {
    private Long id;
    private String title;
    private String subject;
    private String description;
    private RoomVisibility visibility;
    private boolean active;
    private Long ownerId;
    private String ownerName;
    private RoomMemberRole roomRole;
    private boolean liveNow;
}
