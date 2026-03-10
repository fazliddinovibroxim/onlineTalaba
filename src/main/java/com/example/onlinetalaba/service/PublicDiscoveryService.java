package com.example.onlinetalaba.service;

import com.example.onlinetalaba.config.LiveKitProps;
import com.example.onlinetalaba.dto.publicview.PublicLiveLessonResponse;
import com.example.onlinetalaba.dto.publicview.PublicLiveRoomSummaryResponse;
import com.example.onlinetalaba.dto.publicview.PublicRoomResponse;
import com.example.onlinetalaba.entity.LiveSession;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.enums.RoomVisibility;
import com.example.onlinetalaba.repository.LiveSessionRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import io.livekit.server.RoomServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicDiscoveryService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final LiveKitProps liveKitProps;

    public List<PublicRoomResponse> getPublicRooms() {
        List<LiveSession> liveSessions = liveSessionRepository.findAllByActiveTrueAndLessonScheduleRoomVisibility(RoomVisibility.PUBLIC);

        return roomRepository.findAllByVisibilityAndActiveTrue(RoomVisibility.PUBLIC).stream()
                .map(room -> PublicRoomResponse.builder()
                        .roomId(room.getId())
                        .title(room.getTitle())
                        .subject(room.getSubject())
                        .description(room.getDescription())
                        .ownerId(room.getOwner().getId())
                        .ownerName(room.getOwner().getFullName())
                        .memberCount(roomMemberRepository.countByRoomIdAndActiveTrue(room.getId()))
                        .liveNow(liveSessions.stream().anyMatch(session -> session.getLessonSchedule().getRoom().getId().equals(room.getId())))
                        .build())
                .toList();
    }

    public List<PublicLiveLessonResponse> getPublicLiveLessons() {
        return liveSessionRepository.findAllByActiveTrueAndLessonScheduleRoomVisibility(RoomVisibility.PUBLIC).stream()
                .map(this::toLiveLessonResponse)
                .toList();
    }

    private PublicLiveLessonResponse toLiveLessonResponse(LiveSession session) {
        return PublicLiveLessonResponse.builder()
                .liveSessionId(session.getId())
                .lessonScheduleId(session.getLessonSchedule().getId())
                .lessonTitle(session.getLessonSchedule().getTitle())
                .lessonDescription(session.getLessonSchedule().getDescription())
                .teacherName(session.getLessonSchedule().getTeacher().getFullName())
                .startedAt(session.getStartedAt())
                .endTime(session.getLessonSchedule().getEndTime())
                .participantCount(resolveParticipantCount(session))
                .room(PublicLiveRoomSummaryResponse.builder()
                        .roomId(session.getLessonSchedule().getRoom().getId())
                        .title(session.getLessonSchedule().getRoom().getTitle())
                        .subject(session.getLessonSchedule().getRoom().getSubject())
                        .description(session.getLessonSchedule().getRoom().getDescription())
                        .ownerName(session.getLessonSchedule().getRoom().getOwner().getFullName())
                        .build())
                .build();
    }

    private long resolveParticipantCount(LiveSession session) {
        if (liveKitProps.url() == null || liveKitProps.apiKey() == null || liveKitProps.apiSecret() == null) {
            return 0;
        }

        try {
            RoomServiceClient client = RoomServiceClient.create(
                    liveKitProps.url(),
                    liveKitProps.apiKey(),
                    liveKitProps.apiSecret()
            );

            return client.listParticipants(session.getLivekitRoomName())
                    .execute()
                    .body()
                    .size();
        } catch (Exception e) {
            log.warn("Could not resolve live participants for room {}", session.getLivekitRoomName(), e);
            return 0;
        }
    }
}
