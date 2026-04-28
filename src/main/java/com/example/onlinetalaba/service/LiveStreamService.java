package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.live.HandRaiseResponse;
import com.example.onlinetalaba.dto.live.LiveParticipantsResponse;
import com.example.onlinetalaba.dto.stream.StreamWhiteboardToggleCommand;
import com.example.onlinetalaba.entity.LiveSession;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.LiveSessionStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LiveSessionRepository;
import com.example.onlinetalaba.repository.LessonScheduleRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class LiveStreamService {

    private final LiveSessionService liveSessionService;
    private final HandRaiseService handRaiseService;
    private final LiveSessionRepository liveSessionRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final LessonScheduleRepository lessonScheduleRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Transactional
    public LiveSession startLesson(Long liveSessionId, User currentUser) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        if (!canHost(session, currentUser)) {
            throw new ForbiddenException("Only host can start live session");
        }

        if (session.getStatus() != LiveSessionStatus.LIVE || !session.isActive()) {
            session.setStatus(LiveSessionStatus.LIVE);
            session.setActive(true);
            session.setStartedAt(LocalDateTime.now());
            session.setEndedAt(null);
        }

        session.getLessonSchedule().setStatus(com.example.onlinetalaba.enums.LessonStatus.LIVE);
        lessonScheduleRepository.save(session.getLessonSchedule());
        return liveSessionRepository.save(session);
    }

    /**
     * Tracking wrappers (join/heartbeat).
     */
    public void join(Long liveSessionId, User currentUser) {
        liveSessionService.trackJoin(liveSessionId, currentUser);
    }

    public void heartbeat(Long liveSessionId, User currentUser) {
        liveSessionService.trackHeartbeat(liveSessionId, currentUser);
    }

    /**
     * Requirement: getLiveParticipants(sessionId)
     */
    @Transactional(readOnly = true)
    public LiveParticipantsResponse getLiveParticipants(Long liveSessionId) {
        return liveSessionService.getLiveParticipants(liveSessionId);
    }

    /**
     * Requirement: /app/stream/{sessionId}/hand-raise toggles participant flag and notifies teacher.
     * We do NOT duplicate the HandRaise logic; we delegate to existing HandRaiseService.
     */
    @Transactional
    public HandRaiseResponse handRaise(Long liveSessionId, boolean raised, User currentUser) {
        if (raised) {
            return handRaiseService.raiseHand(liveSessionId, currentUser);
        }
        return handRaiseService.cancelHand(liveSessionId, currentUser);
    }

    /**
     * Requirement: toggleWhiteboard(sessionId, boolean) and broadcast command.
     * This does not replace existing /topic/live/{id}/whiteboard flow; it adds /topic/stream/{id}/whiteboard command.
     */
    @Transactional
    public void toggleWhiteboard(Long liveSessionId, boolean enabled, User currentUser) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        if (!canHost(session, currentUser)) {
            throw new ForbiddenException("Only host can toggle whiteboard");
        }

        session.getLessonSchedule().setWhiteboardEnabled(enabled);
        lessonScheduleRepository.save(session.getLessonSchedule());

        messagingTemplate.convertAndSend(
                "/topic/stream/" + liveSessionId + "/whiteboard",
                StreamWhiteboardToggleCommand.builder()
                        .liveSessionId(liveSessionId)
                        .enabled(enabled)
                        .build()
        );
    }

    private boolean canHost(LiveSession session, User currentUser) {
        if (currentUser == null) {
            return false;
        }
        if (currentUser.getRoles() != null && currentUser.getRoles().getAppRoleName() == AppRoleName.SUPER_ADMIN) {
            return true;
        }
        if (session.getHost() != null && session.getHost().getId().equals(currentUser.getId())) {
            return true;
        }

        RoomMember member = roomMemberRepository
                .findByRoomIdAndUserIdAndActiveTrue(session.getLessonSchedule().getRoom().getId(), currentUser.getId())
                .orElse(null);

        return member != null && (member.getRole() == RoomMemberRole.OWNER || member.getRole() == RoomMemberRole.TEACHER);
    }
}

