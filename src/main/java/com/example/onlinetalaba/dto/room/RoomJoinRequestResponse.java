package com.example.onlinetalaba.dto.room;

import com.example.onlinetalaba.enums.RoomJoinRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomJoinRequestResponse {
    private Long id;
    private Long roomId;
    private Long requesterId;
    private String requesterName;
    private String requesterEmail;
    private String message;
    private RoomJoinRequestStatus status;
    private Long processedById;
    private String processedByName;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
