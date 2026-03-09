package com.example.onlinetalaba.dto.log;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserActionLogRequest {

    private String requestedUrl;
    private String actionInfo;
    private String actionType;
}
