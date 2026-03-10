package com.example.onlinetalaba.dto.publicview;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicLiveRoomSummaryResponse {
    private Long roomId;
    private String title;
    private String subject;
    private String description;
    private String ownerName;
}
