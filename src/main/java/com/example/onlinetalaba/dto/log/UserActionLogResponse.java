package com.example.onlinetalaba.dto.log;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserActionLogResponse {

    private Long id;
    private Long userId;
    private String username;
    private String requestedUrl;
    private String actionInfo;
    private String actionType;
    private LocalDateTime time;
}
