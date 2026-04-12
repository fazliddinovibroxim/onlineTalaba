package com.example.onlinetalaba.notification;

import com.example.onlinetalaba.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    public void pushToUser(NotificationDto notification, User user) {
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications",
                notification
        );

        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications/unread-count",
                new NotificationUnreadCountPayload(
                        user.getId(),
                        notificationRepository.countByUserIdAndIsReadFalse(user.getId())
                )
        );
    }
}
