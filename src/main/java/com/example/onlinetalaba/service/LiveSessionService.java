package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.live.LiveKitTokenResponse;
import com.example.onlinetalaba.dto.live.LiveSessionResponse;
import com.example.onlinetalaba.entity.*;
import com.example.onlinetalaba.enums.*;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LiveSessionService {

    private final LessonScheduleRepository lessonScheduleRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final HandRaiseRepository handRaiseRepository;
    private final LiveKitTokenService liveKitTokenService;
    private final RoomService roomService;

    @Transactional
    public LiveSessionResponse createOrStart(Long lessonScheduleId, User currentUser) {
        LessonSchedule lesson = lessonScheduleRepository.findById(lessonScheduleId)
                .orElseThrow(() -> new NotFoundException("Lesson schedule not found"));

        if (!lesson.getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        RoomMember member = roomMemberRepository
                .findByRoomIdAndUserIdAndActiveTrue(lesson.getRoom().getId(), currentUser.getId())
                .orElse(null);

        boolean canStart = (member != null && (member.getRole() == RoomMemberRole.OWNER
                || member.getRole() == RoomMemberRole.TEACHER
                || member.isCanScheduleLesson()))
                || currentUser.getRoles().getAppRoleName() == AppRoleName.SUPER_ADMIN;

        if (!canStart) {
            throw new ForbiddenException("You do not have permission to start live session");
        }

        LiveSession session = liveSessionRepository.findByLessonScheduleId(lessonScheduleId)
                .orElseGet(() -> LiveSession.builder()
                        .lessonSchedule(lesson)
                        .host(currentUser)
                        .livekitRoomName("lesson-" + lesson.getId())
                        .status(LiveSessionStatus.CREATED)
                        .active(true)
                        .build());

        if (session.getStatus() != LiveSessionStatus.LIVE || !session.isActive()) {
            session.setHost(currentUser);
            session.setStatus(LiveSessionStatus.LIVE);
            session.setStartedAt(LocalDateTime.now());
            session.setEndedAt(null);
            session.setActive(true);
        }

        lesson.setStatus(LessonStatus.LIVE);

        liveSessionRepository.save(session);
        lessonScheduleRepository.save(lesson);

        return mapToResponse(session);
    }

    @Transactional(readOnly = true)
    public LiveKitTokenResponse issueToken(Long liveSessionId, User currentUser) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        validateLiveSessionIsJoinable(session);

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        roomService.validateMemberAccess(session.getLessonSchedule().getRoom(), currentUser);

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        session.getLessonSchedule().getRoom().getId(),
                        currentUser.getId())
                .orElse(null);

        boolean canPublish = canPublishAudio(session, member, currentUser);

        String token = liveKitTokenService.createParticipantToken(
                session.getLivekitRoomName(),
                currentUser.getId().toString(),
                currentUser.getFullName(),
                canPublish
        );

        return LiveKitTokenResponse.builder()
                .serverUrl(liveKitTokenService.serverUrl())
                .token(token)
                .roomName(session.getLivekitRoomName())
                .liveSessionId(session.getId())
                .canPublish(canPublish)
                .build();
    }

    @Transactional
    public void end(Long liveSessionId, User currentUser) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        boolean isHost = session.getHost().getId().equals(currentUser.getId());
        boolean isSuperAdmin = currentUser.getRoles().getAppRoleName() == AppRoleName.SUPER_ADMIN;

        if (!isHost && !isSuperAdmin) {
            throw new ForbiddenException("Only host or super admin can end live session");
        }

        session.setStatus(LiveSessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        session.setActive(false);

        LessonSchedule lesson = session.getLessonSchedule();
        lesson.setStatus(LessonStatus.FINISHED);

        liveSessionRepository.save(session);
        lessonScheduleRepository.save(lesson);
    }

    @Transactional(readOnly = true)
    public LiveSessionResponse getByLessonSchedule(Long lessonScheduleId, User currentUser) {
        LiveSession session = liveSessionRepository.findByLessonScheduleId(lessonScheduleId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        roomService.validateMemberAccess(session.getLessonSchedule().getRoom(), currentUser);

        return mapToResponse(session);
    }

    private LiveSessionResponse mapToResponse(LiveSession session) {
        return LiveSessionResponse.builder()
                .id(session.getId())
                .lessonScheduleId(session.getLessonSchedule().getId())
                .hostId(session.getHost().getId())
                .hostName(session.getHost().getFullName())
                .livekitRoomName(session.getLivekitRoomName())
                .status(session.getStatus())
                .active(session.isActive())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .build();
    }

    private boolean canPublishAudio(LiveSession session, RoomMember member, User currentUser) {
        if (currentUser.getRoles().getAppRoleName() == AppRoleName.SUPER_ADMIN) {
            return true;
        }
        
        if (member != null && (member.getRole() == RoomMemberRole.OWNER
                || member.getRole() == RoomMemberRole.TEACHER)) {
            return true;
        }

        // Check if hand is raised and approved
        return handRaiseRepository.existsByLiveSessionIdAndUserIdAndStatus(
                session.getId(), currentUser.getId(), HandRaiseStatus.APPROVED);
    }

    private void validateLiveSessionIsJoinable(LiveSession session) {
        if (!session.isActive() || session.getStatus() != LiveSessionStatus.LIVE) {
            throw new ForbiddenException("Live session is not active");
        }
    }
}
