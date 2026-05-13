// service/RoomNotificationService.java
package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.RoomNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyLessonStarted(Long roomId, Long liveSessionId, Long lessonScheduleId, String lessonTitle) {
        RoomNotificationMessage message = RoomNotificationMessage.builder()
                .type("LESSON_STARTED")
                .title("Jonli dars boshlandi!")
                .body(lessonTitle + " darsi boshlandi. Hozir qo'shiling!")
                .roomId(roomId)
                .liveSessionId(liveSessionId)
                .lessonScheduleId(lessonScheduleId)
                .timestamp(LocalDateTime.now())
                .build();

        broadcast(roomId, message);
    }

    public void notifyLessonEnded(Long roomId, Long liveSessionId, String lessonTitle) {
        RoomNotificationMessage message = RoomNotificationMessage.builder()
                .type("LESSON_ENDED")
                .title("Dars yakunlandi")
                .body(lessonTitle + " darsi yakunlandi.")
                .roomId(roomId)
                .liveSessionId(liveSessionId)
                .timestamp(LocalDateTime.now())
                .build();

        broadcast(roomId, message);
    }

    private void broadcast(Long roomId, RoomNotificationMessage message) {
        String destination = "/topic/room/" + roomId + "/notifications";
        messagingTemplate.convertAndSend(destination, message);
        log.info("Room {} notification sent: {}", roomId, message.getType());
    }
}