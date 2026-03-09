package com.example.onlinetalaba.notification;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationMapper {
    public NotificationDto toDto(Notification notification) {
        if (notification == null) return null;

        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUser() != null ? notification.getUser().getId() : null)
                .title(notification.getTitle())
                .body(notification.getBody())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.getIsRead())
                .build();
    }

    public List<NotificationDto> dtoList(List<Notification> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream().map(this::toDto).toList();
    }
}

