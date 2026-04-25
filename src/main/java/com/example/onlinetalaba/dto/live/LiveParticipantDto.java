package com.example.onlinetalaba.dto.live;

import com.example.onlinetalaba.enums.RoomMemberRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LiveParticipantDto {
    private Long userId;
    private String fullName;
    private String username;
    private RoomMemberRole roomRole;
    private boolean handRaised;
    private boolean online;
    private LocalDateTime joinedAt;
    private LocalDateTime lastSeenAt;
}

