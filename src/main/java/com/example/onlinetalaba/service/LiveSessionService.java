package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.live.LiveKitTokenResponse;
import com.example.onlinetalaba.dto.live.LiveSessionResponse;
import com.example.onlinetalaba.entity.*;
import com.example.onlinetalaba.enums.*;
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
    private final LiveKitTokenService liveKitTokenService;

    @Transactional
    public LiveSessionResponse createOrStart(Long lessonScheduleId, User currentUser) {
        LessonSchedule lesson = lessonScheduleRepository.findById(lessonScheduleId)
                .orElseThrow(() -> new RuntimeException("Lesson schedule not found"));

        RoomMember member = roomMemberRepository
                .findByRoomIdAndUserIdAndActiveTrue(lesson.getRoom().getId(), currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

        boolean canStart = member.getRole() == RoomMemberRole.OWNER
                || member.getRole() == RoomMemberRole.TEACHER
                || member.isCanScheduleLesson();

        if (!canStart) {
            throw new RuntimeException("You do not have permission to start live session");
        }

        LiveSession session = liveSessionRepository.findByLessonScheduleId(lessonScheduleId)
                .orElseGet(() -> LiveSession.builder()
                        .lessonSchedule(lesson)
                        .host(currentUser)
                        .livekitRoomName("lesson-" + lesson.getId())
                        .status(LiveSessionStatus.CREATED)
                        .active(true)
                        .build());

        if (session.getStatus() == LiveSessionStatus.CREATED) {
            session.setStatus(LiveSessionStatus.LIVE);
            session.setStartedAt(LocalDateTime.now());
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
                .orElseThrow(() -> new RuntimeException("Live session not found"));

        roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        session.getLessonSchedule().getRoom().getId(),
                        currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

        String token = liveKitTokenService.createParticipantToken(
                session.getLivekitRoomName(),
                currentUser.getId().toString(),
                currentUser.getFullName()
        );

        return LiveKitTokenResponse.builder()
                .serverUrl(liveKitTokenService.serverUrl())
                .token(token)
                .roomName(session.getLivekitRoomName())
                .liveSessionId(session.getId())
                .build();
    }

    @Transactional
    public void end(Long liveSessionId, User currentUser) {
        LiveSession session = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new RuntimeException("Live session not found"));

        if (!session.getHost().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only host can end live session");
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
                .orElseThrow(() -> new RuntimeException("Live session not found"));

        roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        session.getLessonSchedule().getRoom().getId(),
                        currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

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
}