package com.example.onlinetalaba.dto.room;

import com.example.onlinetalaba.enums.RoomVisibility;
import lombok.Builder;
import lombok.Data;

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
}