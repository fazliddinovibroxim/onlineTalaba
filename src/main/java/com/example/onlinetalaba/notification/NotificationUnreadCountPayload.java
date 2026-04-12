package com.example.onlinetalaba.notification;

public record NotificationUnreadCountPayload(
        Long userId,
        long unreadCount
) {}
