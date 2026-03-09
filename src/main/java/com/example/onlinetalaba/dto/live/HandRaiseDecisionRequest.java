package com.example.onlinetalaba.dto.live;

import com.example.onlinetalaba.enums.HandRaiseStatus;
import lombok.Data;

@Data
public class HandRaiseDecisionRequest {
    private Long handRaiseId;
    private HandRaiseStatus status; // APPROVED / REJECTED / FINISHED
}