package com.example.onlinetalaba.dto.dashboard;

import com.example.onlinetalaba.enums.RoomJoinRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DashboardJoinRequestResponse {
    private Long id;
    private Long roomId;
    private String roomTitle;
    private Long requesterId;
    private String requesterName;
    private String requesterEmail;
    private RoomJoinRequestStatus status;
    private String message;
    private LocalDateTime createdAt;
}
