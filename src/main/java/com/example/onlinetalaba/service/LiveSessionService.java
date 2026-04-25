package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.live.LiveKitTokenResponse;
import com.example.onlinetalaba.dto.live.LiveParticipantDto;
import com.example.onlinetalaba.dto.live.LiveParticipantsResponse;
import com.example.onlinetalaba.dto.live.LiveSessionResponse;
import com.example.onlinetalaba.entity.*;
import com.example.onlinetalaba.enums.*;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiveSessionService {

    private static final Duration OFFLINE_AFTER = Duration.ofSeconds(90);

    private final LessonScheduleRepository lessonScheduleRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final HandRaiseRepository handRaiseRepository;
    private final LiveSessionParticipantRepository liveSessionParticipantRepository;
    private final LiveKitTokenService liveKitTokenService;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

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

    /**
     * WebSocket: /app/stream/{liveSessionId}/join
     */
    @Transactional
    public void trackJoin(Long liveSessionId, User currentUser) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }
        validateLiveSessionIsJoinable(session);
        roomService.validateMemberAccess(session.getLessonSchedule().getRoom(), currentUser);

        upsertParticipant(session, currentUser, null, true);
        broadcastParticipants(liveSessionId);
    }

    /**
     * WebSocket: /app/stream/{liveSessionId}/heartbeat
     */
    @Transactional
    public void trackHeartbeat(Long liveSessionId, User currentUser) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }
        validateLiveSessionIsJoinable(session);
        roomService.validateMemberAccess(session.getLessonSchedule().getRoom(), currentUser);

        upsertParticipant(session, currentUser, null, false);
        broadcastParticipants(liveSessionId);
    }

    /**
     * Called from HandRaise flow to keep participants list in sync without duplicating endpoints.
     */
    @Transactional
    public void updateParticipantHandRaised(Long liveSessionId, User user, boolean handRaised) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        if (!session.getLessonSchedule().getRoom().isActive()) {
            throw new ForbiddenException("Room is not active");
        }
        if (!session.isActive() || session.getStatus() != LiveSessionStatus.LIVE) {
            return;
        }

        upsertParticipant(session, user, handRaised, true);
        broadcastParticipants(liveSessionId);
    }

    @Transactional(readOnly = true)
    public LiveParticipantsResponse getLiveParticipants(Long liveSessionId) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new NotFoundException("Live session not found"));

        Long roomId = session.getLessonSchedule().getRoom().getId();
        Map<Long, RoomMemberRole> rolesByUserId = new HashMap<>();
        for (RoomMember m : roomMemberRepository.findAllByRoomIdAndActiveTrue(roomId)) {
            rolesByUserId.put(m.getUser().getId(), m.getRole());
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime onlineAfter = now.minusSeconds(OFFLINE_AFTER.toSeconds());

        List<LiveParticipantDto> participants = liveSessionParticipantRepository
                .findAllByLiveSessionIdOrderByJoinedAtAsc(liveSessionId)
                .stream()
                .map(p -> LiveParticipantDto.builder()
                        .userId(p.getUser().getId())
                        .fullName(p.getUser().getFullName())
                        .username(p.getUser().getUsername())
                        .roomRole(rolesByUserId.getOrDefault(p.getUser().getId(), null))
                        .handRaised(p.isHandRaised())
                        .online(p.getLastSeenAt() != null && !p.getLastSeenAt().isBefore(onlineAfter))
                        .joinedAt(p.getJoinedAt())
                        .lastSeenAt(p.getLastSeenAt())
                        .build())
                .toList();

        long onlineCount = participants.stream().filter(LiveParticipantDto::isOnline).count();
        return LiveParticipantsResponse.builder()
                .liveSessionId(liveSessionId)
                .onlineCount(onlineCount)
                .serverTime(now)
                .participants(participants)
                .build();
    }

    public void broadcastParticipants(Long liveSessionId) {
        LiveParticipantsResponse response = getLiveParticipants(liveSessionId);
        messagingTemplate.convertAndSend("/topic/stream/" + liveSessionId + "/participants", response);
    }

    private void upsertParticipant(LiveSession session, User user, Boolean handRaised, boolean touchJoinedAt) {
        LiveSessionParticipant participant = liveSessionParticipantRepository
                .findByLiveSessionIdAndUserId(session.getId(), user.getId())
                .orElseGet(() -> LiveSessionParticipant.builder()
                        .liveSession(session)
                        .user(user)
                        .joinedAt(LocalDateTime.now())
                        .handRaised(false)
                        .lastSeenAt(LocalDateTime.now())
                        .build());

        if (touchJoinedAt && participant.getJoinedAt() == null) {
            participant.setJoinedAt(LocalDateTime.now());
        }

        participant.setLastSeenAt(LocalDateTime.now());
        if (handRaised != null) {
            participant.setHandRaised(handRaised);
        }

        liveSessionParticipantRepository.save(participant);
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
