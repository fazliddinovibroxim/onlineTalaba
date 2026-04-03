package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.publicview.PrivateRoomResponse;
import com.example.onlinetalaba.dto.publicview.PublicLessonMiniResponse;
import com.example.onlinetalaba.dto.publicview.PublicLiveLessonResponse;
import com.example.onlinetalaba.dto.publicview.PublicLiveRoomSummaryResponse;
import com.example.onlinetalaba.dto.publicview.PublicMaterialMiniResponse;
import com.example.onlinetalaba.dto.publicview.PublicRoomRichResponse;
import com.example.onlinetalaba.dto.publicview.PublicSubjectSummaryResponse;
import com.example.onlinetalaba.entity.LibraryMaterial;
import com.example.onlinetalaba.entity.LiveSession;
import com.example.onlinetalaba.entity.LessonSchedule;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.LessonStatus;
import com.example.onlinetalaba.enums.RoomJoinRequestStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.enums.RoomVisibility;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LibraryMaterialRepository;
import com.example.onlinetalaba.repository.LiveSessionRepository;
import com.example.onlinetalaba.repository.LessonScheduleRepository;
import com.example.onlinetalaba.repository.RoomJoinRequestRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCatalogService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final LessonScheduleRepository lessonScheduleRepository;
    private final LibraryMaterialRepository libraryMaterialRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final RoomJoinRequestRepository roomJoinRequestRepository;

    public List<PublicRoomRichResponse> getPublicRooms(User currentUser) {
        List<Room> publicRooms = roomRepository.findAllByVisibilityAndActiveTrue(RoomVisibility.PUBLIC);
        Set<Long> myRoomIds = getMyRoomIds(currentUser);

        List<Room> visibleRooms = isTeacherOrStudent(currentUser)
                ? publicRooms.stream().filter(room -> myRoomIds.contains(room.getId())).toList()
                : publicRooms;

        return visibleRooms.stream()
                .map(room -> toPublicRoomRich(room, currentUser))
                .toList();
    }

    public List<PrivateRoomResponse> getPrivateRooms(User currentUser) {
        List<Room> privateRooms = roomRepository.findAllByVisibilityAndActiveTrue(RoomVisibility.PRIVATE);

        if (isSuperScope(currentUser)) {
            return privateRooms.stream()
                    .map(room -> toPrivateRoomRich(room, currentUser))
                    .toList();
        }

        Set<Long> myRoomIds = getMyRoomIds(currentUser);

        return privateRooms.stream()
                .filter(room -> myRoomIds.contains(room.getId()))
                .map(room -> toPrivateRoomRich(room, currentUser))
                .toList();
    }

    public PublicRoomRichResponse getPublicRoomPreview(Long roomId, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        if (!room.isActive() || room.getVisibility() != RoomVisibility.PUBLIC) {
            throw new NotFoundException("Public room not found");
        }

        if (isTeacherOrStudent(currentUser)) {
            boolean isMember = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(room.getId(), currentUser.getId()).isPresent();
            if (!isMember) {
                throw new ForbiddenException("You can only view your own public rooms");
            }
        }

        return toPublicRoomRich(room, currentUser);
    }

    public List<PublicLiveLessonResponse> getPublicLiveLessons(User currentUser) {
        List<LiveSession> sessions = liveSessionRepository.findAllByActiveTrueAndLessonScheduleRoomVisibility(RoomVisibility.PUBLIC);
        Set<Long> myRoomIds = getMyRoomIds(currentUser);

        List<LiveSession> visibleSessions = isTeacherOrStudent(currentUser)
                ? sessions.stream().filter(s -> myRoomIds.contains(s.getLessonSchedule().getRoom().getId())).toList()
                : sessions;

        return visibleSessions.stream()
                .map(this::toLiveLessonResponse)
                .toList();
    }

    public List<PublicSubjectSummaryResponse> getSubjectSummary(User currentUser) {
        List<PublicRoomRichResponse> rooms = getPublicRooms(currentUser);

        return rooms.stream()
                .collect(Collectors.groupingBy(PublicRoomRichResponse::getSubject))
                .entrySet()
                .stream()
                .map(entry -> PublicSubjectSummaryResponse.builder()
                        .subject(entry.getKey())
                        .roomCount(entry.getValue().size())
                        .liveRoomCount(entry.getValue().stream().filter(PublicRoomRichResponse::isLiveNow).count())
                        .weeklyLessonCount(entry.getValue().stream().mapToLong(PublicRoomRichResponse::getWeeklyLessonCount).sum())
                        .activeMemberCount(entry.getValue().stream().mapToLong(PublicRoomRichResponse::getMemberCount).sum())
                        .build())
                .sorted(Comparator.comparing(PublicSubjectSummaryResponse::getSubject, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private PublicRoomRichResponse toPublicRoomRich(Room room, User currentUser) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusDays(7);

        List<PublicLessonMiniResponse> upcomingLessons = lessonScheduleRepository
                .findTop3ByRoomIdAndStartTimeAfterOrderByStartTimeAsc(room.getId(), now)
                .stream()
                .map(this::toLessonMini)
                .toList();

        List<PublicMaterialMiniResponse> recentMaterials = libraryMaterialRepository
                .findTop5ByRoomIdAndActiveTrueOrderByDatetimeCreatedDesc(room.getId())
                .stream()
                .map(this::toMaterialMini)
                .toList();

        LocalDateTime lastLessonAt = lessonScheduleRepository.findTop1ByRoomIdOrderByStartTimeDesc(room.getId())
                .stream()
                .findFirst()
                .map(LessonSchedule::getStartTime)
                .orElse(null);

        LocalDateTime lastMaterialAt = libraryMaterialRepository.findTopByRoomIdAndActiveTrueOrderByDatetimeCreatedDesc(room.getId())
                .map(LibraryMaterial::getDatetimeCreated)
                .orElse(null);

        LocalDateTime lastLiveUpdatedAt = liveSessionRepository.findTopByLessonScheduleRoomIdOrderByDatetimeUpdatedDesc(room.getId())
                .map(LiveSession::getDatetimeUpdated)
                .orElse(null);

        LocalDateTime lastActiveAt = maxTime(lastLessonAt, lastMaterialAt, lastLiveUpdatedAt);

        long activeLessonCount = lessonScheduleRepository.countByRoomIdAndStatus(room.getId(), LessonStatus.LIVE);
        long weeklyLessonCount = lessonScheduleRepository.countByRoomIdAndStatusAndStartTimeBetween(room.getId(), LessonStatus.SCHEDULED, now, weekLater)
                + lessonScheduleRepository.countByRoomIdAndStatusAndStartTimeBetween(room.getId(), LessonStatus.LIVE, now, weekLater);

        boolean liveNow = liveSessionRepository.existsByLessonScheduleRoomIdAndActiveTrue(room.getId());

        return PublicRoomRichResponse.builder()
                .roomId(room.getId())
                .title(room.getTitle())
                .subject(room.getSubject())
                .description(room.getDescription())
                .visibility(room.getVisibility())
                .active(room.isActive())
                .ownerId(room.getOwner().getId())
                .ownerName(room.getOwner().getFullName())
                .memberCount(roomMemberRepository.countByRoomIdAndActiveTrue(room.getId()))
                .teacherCount(1 + roomMemberRepository.countByRoomIdAndActiveTrueAndRole(room.getId(), RoomMemberRole.TEACHER))
                .activeLessonCount(activeLessonCount)
                .weeklyLessonCount(weeklyLessonCount)
                .resourceCount(libraryMaterialRepository.countByRoomIdAndActiveTrue(room.getId()))
                .lastLessonAt(lastLessonAt)
                .lastMaterialAt(lastMaterialAt)
                .lastActiveAt(lastActiveAt)
                .liveNow(liveNow)
                .upcomingLessons(upcomingLessons)
                .recentMaterials(recentMaterials)
                .outcomes(new ArrayList<>())
                .level(inferLevel(room))
                .prerequisites(new ArrayList<>())
                .tags(buildTags(room))
                .language("uz")
                .priceType("FREE")
                .canJoinDirectly(room.getVisibility() == RoomVisibility.PUBLIC && room.isActive())
                .requiresApproval(room.getVisibility() == RoomVisibility.PRIVATE)
                .nextIntakeDate(upcomingLessons.isEmpty() ? null : upcomingLessons.get(0).getStartTime())
                .joinCount30d(roomJoinRequestRepository.countByRoomIdAndStatusAndDatetimeCreatedAfter(
                        room.getId(),
                        RoomJoinRequestStatus.APPROVED,
                        now.minusDays(30)
                ))
                .reviewsCount(null)
                .completionRate(null)
                .build();
    }

    private PrivateRoomResponse toPrivateRoomRich(Room room, User currentUser) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusDays(7);

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(room.getId(), currentUser.getId()).orElse(null);

        boolean canModerate = isSuperScope(currentUser)
                || (member != null && (member.getRole() == RoomMemberRole.OWNER || member.getRole() == RoomMemberRole.TEACHER));

        List<PublicLessonMiniResponse> upcomingLessons = lessonScheduleRepository
                .findTop3ByRoomIdAndStartTimeAfterOrderByStartTimeAsc(room.getId(), now)
                .stream()
                .map(this::toLessonMini)
                .toList();

        List<PublicMaterialMiniResponse> recentMaterials = libraryMaterialRepository
                .findTop5ByRoomIdAndActiveTrueOrderByDatetimeCreatedDesc(room.getId())
                .stream()
                .map(this::toMaterialMini)
                .toList();

        LocalDateTime lastLessonAt = lessonScheduleRepository.findTop1ByRoomIdOrderByStartTimeDesc(room.getId())
                .stream()
                .findFirst()
                .map(LessonSchedule::getStartTime)
                .orElse(null);

        LocalDateTime lastMaterialAt = libraryMaterialRepository.findTopByRoomIdAndActiveTrueOrderByDatetimeCreatedDesc(room.getId())
                .map(LibraryMaterial::getDatetimeCreated)
                .orElse(null);

        LocalDateTime lastLiveUpdatedAt = liveSessionRepository.findTopByLessonScheduleRoomIdOrderByDatetimeUpdatedDesc(room.getId())
                .map(LiveSession::getDatetimeUpdated)
                .orElse(null);

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
                .teacherCount(1 + roomMemberRepository.countByRoomIdAndActiveTrueAndRole(room.getId(), RoomMemberRole.TEACHER))
                .activeLessonCount(lessonScheduleRepository.countByRoomIdAndStatus(room.getId(), LessonStatus.LIVE))
                .weeklyLessonCount(lessonScheduleRepository.countByRoomIdAndStatusAndStartTimeBetween(room.getId(), LessonStatus.SCHEDULED, now, weekLater)
                        + lessonScheduleRepository.countByRoomIdAndStatusAndStartTimeBetween(room.getId(), LessonStatus.LIVE, now, weekLater))
                .resourceCount(libraryMaterialRepository.countByRoomIdAndActiveTrue(room.getId()))
                .liveNow(liveSessionRepository.existsByLessonScheduleRoomIdAndActiveTrue(room.getId()))
                .pendingJoinRequestCount(canModerate
                        ? roomJoinRequestRepository.countByRoomIdAndStatus(room.getId(), RoomJoinRequestStatus.PENDING)
                        : 0)
                .joinCount30d(canModerate
                        ? roomJoinRequestRepository.countByRoomIdAndStatusAndDatetimeCreatedAfter(room.getId(), RoomJoinRequestStatus.APPROVED, now.minusDays(30))
                        : 0)
                .createdAt(room.getDatetimeCreated())
                .updatedAt(room.getDatetimeUpdated())
                .lastLessonAt(lastLessonAt)
                .lastMaterialAt(lastMaterialAt)
                .lastActiveAt(maxTime(lastLessonAt, lastMaterialAt, lastLiveUpdatedAt))
                .upcomingLessons(upcomingLessons)
                .recentMaterials(recentMaterials)
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

    private PublicLessonMiniResponse toLessonMini(LessonSchedule lesson) {
        return PublicLessonMiniResponse.builder()
                .lessonId(lesson.getId())
                .title(lesson.getTitle())
                .teacherName(lesson.getTeacher().getFullName())
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .build();
    }

    private PublicMaterialMiniResponse toMaterialMini(LibraryMaterial material) {
        return PublicMaterialMiniResponse.builder()
                .materialId(material.getId())
                .title(material.getTitle())
                .materialType(material.getMaterialType())
                .uploadedBy(material.getUploadedBy().getFullName())
                .uploadedAt(material.getDatetimeCreated())
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
                .participantCount(roomMemberRepository.countByRoomIdAndActiveTrue(session.getLessonSchedule().getRoom().getId()))
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

    private List<String> buildTags(Room room) {
        List<String> tags = new ArrayList<>();
        if (room.getSubject() != null && !room.getSubject().isBlank()) {
            tags.add(room.getSubject().trim().toLowerCase(Locale.ROOT));
        }
        if (room.getTitle() != null && !room.getTitle().isBlank()) {
            tags.add(room.getTitle().trim().toLowerCase(Locale.ROOT));
        }
        return tags.stream().distinct().toList();
    }

    private String inferLevel(Room room) {
        String text = ((room.getSubject() == null ? "" : room.getSubject()) + " "
                + (room.getDescription() == null ? "" : room.getDescription())).toLowerCase(Locale.ROOT);

        if (text.contains("advanced") || text.contains("expert") || text.contains("pro")) {
            return "ADVANCED";
        }
        if (text.contains("intermediate") || text.contains("middle")) {
            return "INTERMEDIATE";
        }
        if (text.contains("beginner") || text.contains("start") || text.contains("basic")) {
            return "BEGINNER";
        }
        return "GENERAL";
    }

    private LocalDateTime maxTime(LocalDateTime a, LocalDateTime b, LocalDateTime c) {
        return List.of(a, b, c).stream()
                .filter(v -> v != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}
