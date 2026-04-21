package com.example.onlinetalaba.service;

import com.example.onlinetalaba.config.LiveKitProps;
import com.example.onlinetalaba.dto.publicview.PrivateRoomResponse;
import com.example.onlinetalaba.dto.publicview.PublicLiveLessonResponse;
import com.example.onlinetalaba.dto.publicview.PublicLiveRoomSummaryResponse;
import com.example.onlinetalaba.dto.publicview.PublicRoomResponse;
import com.example.onlinetalaba.entity.LiveSession;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.RoomJoinRequestStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.enums.RoomVisibility;
import com.example.onlinetalaba.repository.LiveSessionRepository;
import com.example.onlinetalaba.repository.RoomJoinRequestRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import io.livekit.server.RoomServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicDiscoveryService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final RoomJoinRequestRepository roomJoinRequestRepository;
    private final LiveKitProps liveKitProps;

    public List<PublicRoomResponse> getPublicRooms(User currentUser) {
        List<Room> publicRooms = roomRepository.findAllByVisibilityAndActiveTrue(RoomVisibility.PUBLIC);
        Set<Long> myRoomIds = getMyRoomIds(currentUser);

        List<Room> filteredRooms = publicRooms;

        return filteredRooms.stream()
                .map(this::toPublicRoomResponse)
                .toList();
    }

    public List<PrivateRoomResponse> getPrivateRooms(User currentUser) {
        List<Room> privateRooms = roomRepository.findAllByVisibilityAndActiveTrue(RoomVisibility.PRIVATE);

        return privateRooms.stream()
                .map(room -> toPrivateRoomResponse(room, currentUser))
                .toList();
    }

    public List<PublicLiveLessonResponse> getPublicLiveLessons(User currentUser) {
        List<LiveSession> sessions = liveSessionRepository.findAllByActiveTrueAndLessonScheduleRoomVisibility(RoomVisibility.PUBLIC);
        Set<Long> myRoomIds = getMyRoomIds(currentUser);

        List<LiveSession> filtered = isTeacherOrStudent(currentUser)
                ? sessions.stream()
                .filter(session -> myRoomIds.contains(session.getLessonSchedule().getRoom().getId()))
                .toList()
                : sessions;

        return filtered.stream()
                .map(this::toLiveLessonResponse)
                .toList();
    }

    private PublicRoomResponse toPublicRoomResponse(Room room) {
        return PublicRoomResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .subject(room.getSubject())
                .description(room.getDescription())
                .ownerId(room.getOwner().getId())
                .ownerName(room.getOwner().getFullName())
                .memberCount(roomMemberRepository.countByRoomIdAndActiveTrue(room.getId()))
                .liveNow(liveSessionRepository.existsByLessonScheduleRoomIdAndActiveTrue(room.getId()))
                .build();
    }

    private PrivateRoomResponse toPrivateRoomResponse(Room room, User currentUser) {
        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(room.getId(), currentUser.getId()).orElse(null);
        boolean canModerate = member != null && (member.getRole() == RoomMemberRole.OWNER || member.getRole() == RoomMemberRole.TEACHER);

        return PrivateRoomResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .subject(room.getSubject())
                .description(room.getDescription())
                .visibility(room.getVisibility())
                .active(room.isActive())
                .ownerId(room.getOwner().getId())
                .ownerName(room.getOwner().getFullName())
                .ownerEmail(room.getOwner().getEmail())
                .memberCount(roomMemberRepository.countByRoomIdAndActiveTrue(room.getId()))
                .liveNow(liveSessionRepository.existsByLessonScheduleRoomIdAndActiveTrue(room.getId()))
                .pendingJoinRequestCount(canModerate
                        ? roomJoinRequestRepository.countByRoomIdAndStatus(room.getId(), RoomJoinRequestStatus.PENDING)
                        : 0)
                .createdAt(room.getDatetimeCreated())
                .updatedAt(room.getDatetimeUpdated())
                .myRole(member == null ? null : member.getRole())
                .canManageRoom(member != null && (member.getRole() == RoomMemberRole.OWNER || member.isCanManageRoom()))
                .canInviteMembers(member != null && (member.getRole() == RoomMemberRole.OWNER || member.isCanInviteMembers()))
                .canScheduleLesson(member != null && (member.getRole() == RoomMemberRole.OWNER || member.isCanScheduleLesson()))
                .canUploadMaterials(member != null && (member.getRole() == RoomMemberRole.OWNER || member.isCanUploadMaterials()))
                .myPendingJoinRequest(roomJoinRequestRepository.existsByRoomIdAndRequesterIdAndStatus(
                        room.getId(),
                        currentUser.getId(),
                        RoomJoinRequestStatus.PENDING
                ))
                .build();
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

    private Set<Long> getMyRoomIds(User currentUser) {
        return roomMemberRepository.findAllByUserIdAndActiveTrue(currentUser.getId()).stream()
                .map(member -> member.getRoom().getId())
                .collect(Collectors.toSet());
    }

    private boolean isSuperScope(User currentUser) {
        AppRoleName role = currentUser.getRoles().getAppRoleName();
        return role == AppRoleName.SUPER_ADMIN || role == AppRoleName.ADMIN;
    }

    private boolean isTeacherOrStudent(User currentUser) {
        AppRoleName role = currentUser.getRoles().getAppRoleName();
        return role == AppRoleName.TEACHER || role == AppRoleName.STUDENT;
    }

    private long resolveParticipantCount(LiveSession session) {
        if (liveKitProps.apiBaseUrl() == null || liveKitProps.apiKey() == null || liveKitProps.apiSecret() == null) {
            return 0;
        }

        try {
            RoomServiceClient client = RoomServiceClient.create(
                    liveKitProps.apiBaseUrl(),
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
