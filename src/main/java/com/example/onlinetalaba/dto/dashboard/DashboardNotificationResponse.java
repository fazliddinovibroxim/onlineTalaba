package com.example.onlinetalaba.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DashboardNotificationResponse {
    private Long id;
    private String title;
    private String body;
    private String status;
    private LocalDate createdAt;
    private Boolean isRead;
}
