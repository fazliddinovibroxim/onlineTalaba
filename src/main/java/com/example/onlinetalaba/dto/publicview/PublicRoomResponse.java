package com.example.onlinetalaba.dto.publicview;

import com.example.onlinetalaba.enums.UserGender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicRoomResponse {
    private Long roomId;
    private String title;
    private String subject;
    private String description;
    private Long ownerId;
    private String ownerName;
    private UserGender ownerGender;
    private long memberCount;
    private boolean liveNow;
}
