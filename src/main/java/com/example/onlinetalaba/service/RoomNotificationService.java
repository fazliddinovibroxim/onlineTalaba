package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.RoomNotificationMessage;
import com.example.onlinetalaba.dto.live.LiveSessionCommentResponse;
import com.example.onlinetalaba.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomNotificationService {

    private static final int COMMENT_PREVIEW_MAX = 120;

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyLessonStarted(Long roomId, Long liveSessionId, Long lessonScheduleId, String lessonTitle) {
        String safeTitle = lessonTitle != null && !lessonTitle.isBlank() ? lessonTitle : "Jonli dars";

        RoomNotificationMessage message = RoomNotificationMessage.builder()
                .type("LESSON_STARTED")
                .title("Jonli translatsiya boshlandi")
                .body("«" + safeTitle + "» darsiga qo'shiling! Jonli stream ochiq.")
                .roomId(roomId)
                .liveSessionId(liveSessionId)
                .lessonScheduleId(lessonScheduleId)
                .timestamp(LocalDateTime.now())
                .build();

        broadcast(roomId, message);
    }

    public void notifyLessonEnded(Long roomId, Long liveSessionId, String lessonTitle) {
        String safeTitle = lessonTitle != null && !lessonTitle.isBlank() ? lessonTitle : "Jonli dars";

        RoomNotificationMessage message = RoomNotificationMessage.builder()
                .type("LESSON_ENDED")
                .title("Dars yakunlandi")
                .body("«" + safeTitle + "» darsi yakunlandi.")
                .roomId(roomId)
                .liveSessionId(liveSessionId)
                .timestamp(LocalDateTime.now())
                .build();

        broadcast(roomId, message);
    }

    public void notifyStreamMemberJoined(
            Long roomId,
            Long liveSessionId,
            Long lessonScheduleId,
            String lessonTitle,
            User member
    ) {
        String memberName = displayName(member);
        String safeTitle = lessonTitle != null && !lessonTitle.isBlank() ? lessonTitle : "Jonli dars";

        RoomNotificationMessage message = RoomNotificationMessage.builder()
                .type("STREAM_MEMBER_JOINED")
                .title(memberName + " jonli darsga qo'shildi")
                .body(memberName + " «" + safeTitle + "» jonli streamiga qo'shildi.")
                .roomId(roomId)
                .liveSessionId(liveSessionId)
                .lessonScheduleId(lessonScheduleId)
                .actorUserId(member.getId())
                .actorUserName(memberName)
                .timestamp(LocalDateTime.now())
                .build();

        broadcast(roomId, message);
    }

    public void notifyStreamComment(
            Long roomId,
            Long liveSessionId,
            Long lessonScheduleId,
            User sender,
            LiveSessionCommentResponse comment
    ) {
        String senderName = displayName(sender);
        String preview = preview(comment.getContent());

        RoomNotificationMessage message = RoomNotificationMessage.builder()
                .type("STREAM_COMMENT")
                .title(senderName + " izoh yozdi")
                .body(preview)
                .roomId(roomId)
                .liveSessionId(liveSessionId)
                .lessonScheduleId(lessonScheduleId)
                .actorUserId(sender.getId())
                .actorUserName(senderName)
                .commentId(comment.getId())
                .timestamp(LocalDateTime.now())
                .build();

        broadcast(roomId, message);
    }

    private void broadcast(Long roomId, RoomNotificationMessage message) {
        String destination = "/topic/room/" + roomId + "/notifications";
        messagingTemplate.convertAndSend(destination, message);
        log.info("Room {} notification sent: {}", roomId, message.getType());
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        return user.getUsername();
    }

    private String preview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String trimmed = content.trim();
        if (trimmed.length() <= COMMENT_PREVIEW_MAX) {
            return trimmed;
        }
        return trimmed.substring(0, COMMENT_PREVIEW_MAX) + "...";
    }
}