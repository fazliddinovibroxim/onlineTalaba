package com.example.onlinetalaba.dto.live;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LiveParticipantsResponse {
    private Long liveSessionId;
    private long onlineCount;
    private LocalDateTime serverTime;
    private List<LiveParticipantDto> participants;
}

