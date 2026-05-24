package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.auth.UserDashboardResponse;
import com.example.onlinetalaba.dto.auth.UserDto;
import com.example.onlinetalaba.dto.dashboard.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.example.onlinetalaba.entity.*;
import com.example.onlinetalaba.enums.AppPermissions;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.LessonStatus;
import com.example.onlinetalaba.enums.RoomJoinRequestStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.enums.RoomVisibility;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.notification.Notification;
import com.example.onlinetalaba.notification.NotificationRepository;
import com.example.onlinetalaba.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDashboardService {

    private final UserRepository userRepository;
    private final UserDashboardRepository userDashboardRepository;
    private final ChatRoomService chatRoomService;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final LessonScheduleRepository lessonScheduleRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final LibraryMaterialRepository libraryMaterialRepository;
    private final NotificationRepository notificationRepository;
    private final RoomJoinRequestRepository roomJoinRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional(readOnly = true)
    public Page<UserSearchItemResponse> searchUsers(String q,
                                                    String fullName,
                                                    String username,
                                                    String email,
                                                    String phoneNumber,
                                                    String address,
                                                    Long excludeUserId,
                                                    Pageable pageable) {
        String normalizedQ = normalizeFilter(q);
        String normalizedFullName = normalizeFilter(fullName);
        String normalizedUsername = normalizeFilter(username);
        String normalizedEmail = normalizeFilter(email);
        String normalizedPhone = normalizeFilter(phoneNumber);
        String normalizedAddress = normalizeFilter(address);

        long total = userDashboardRepository.countByNativeQuery(
                normalizedQ,
                normalizedFullName,
                normalizedUsername,
                normalizedEmail,
                normalizedPhone,
                normalizedAddress,
                excludeUserId
        );

        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<Object[]> rows = userDashboardRepository.findAllByNativeQuery(
                normalizedQ,
                normalizedFullName,
                normalizedUsername,
                normalizedEmail,
                normalizedPhone,
                normalizedAddress,
                excludeUserId,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        List<UserSearchItemResponse> content = rows.stream()
                .map(this::mapUserSearchRow)
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    @Transactional(readOnly = true)
    public UserDashboardResponse getUserDashboard(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Role role = roleRepository.findByAppRoleName(user.getRoles().getAppRoleName())
                .orElseThrow(() -> new NotFoundException("Role not found for user"));

        AppRoleName appRole = role.getAppRoleName();
        Set<AppPermissions> permissions = role.getAppPermissions();

        if (appRole == AppRoleName.SUPER_ADMIN) {
            return buildSuperAdminDashboard(user, permissions);
        }

        List<RoomMember> memberships = roomMemberRepository.findAllByUserIdAndActiveTrue(user.getId()).stream()
                .filter(m -> m.getRoom() != null && m.getRoom().isActive())
                .toList();
        List<Room> myRooms = memberships.stream()
                .map(RoomMember::getRoom)
                .distinct()
                .toList();
        List<Long> roomIds = myRooms.stream().map(Room::getId).toList();

        List<LessonSchedule> upcomingLessons = roomIds.isEmpty()
                ? Collections.emptyList()
                : lessonScheduleRepository.findAllByRoomIdInAndStartTimeAfterOrderByStartTimeAsc(roomIds, LocalDateTime.now());
        List<LiveSession> liveSessions = roomIds.isEmpty()
                ? Collections.emptyList()
                : liveSessionRepository.findAllByLessonScheduleRoomIdInAndActiveTrue(roomIds);
        List<LibraryMaterial> recentMaterials = roomIds.isEmpty()
                ? Collections.emptyList()
                : libraryMaterialRepository.findTop10ByRoomIdInAndActiveTrueOrderByDatetimeCreatedDesc(roomIds);
        List<Notification> notifications = notificationRepository.findTop10ByUserIdOrderByIdDesc(user.getId());
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        List<RoomJoinRequest> myJoinRequests = roomJoinRequestRepository.findAllByRequesterIdOrderByDatetimeCreatedDesc(user.getId()).stream()
                .filter(jr -> jr.getRoom() != null && jr.getRoom().isActive())
                .toList();

        List<Long> ownedRoomIds = roomRepository.findByOwnerId(user.getId()).stream()
                .filter(Room::isActive)
                .map(Room::getId)
                .distinct()
                .sorted()
                .toList();

        List<Long> memberRoomIds = memberships.stream()
                .map(m -> m.getRoom().getId())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        List<Long> memberPublicRoomIds = memberships.stream()
                .filter(m -> m.getRoom().getVisibility() == RoomVisibility.PUBLIC)
                .map(m -> m.getRoom().getId())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        List<Long> memberPrivateRoomIds = memberships.stream()
                .filter(m -> m.getRoom().getVisibility() == RoomVisibility.PRIVATE)
                .map(m -> m.getRoom().getId())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        List<Long> joinRequestRoomIdsSent = myJoinRequests.stream()
                .map(jr -> jr.getRoom().getId())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        List<com.example.onlinetalaba.dto.chatroom.ChatRoomMeResponse> chatRooms =
                chatRoomService.listMyRoomsForDashboard(user);

        List<RoomJoinRequest> moderatedRequests = memberships.stream()
                .filter(member -> member.getRole().name().equals("OWNER") || member.getRole().name().equals("TEACHER"))
                .flatMap(member -> roomJoinRequestRepository
                        .findAllByRoomIdAndStatusOrderByDatetimeCreatedAsc(member.getRoom().getId(), RoomJoinRequestStatus.PENDING)
                        .stream())
                .distinct()
                .toList();
        boolean canModerateRoomRequests = !moderatedRequests.isEmpty()
                || memberships.stream().anyMatch(member -> member.getRole().name().equals("OWNER") || member.getRole().name().equals("TEACHER"));

        List<Room> discoverRooms = (appRole == AppRoleName.STUDENT || appRole == AppRoleName.TEACHER || appRole == AppRoleName.USER)
                ? roomRepository.findAll().stream()
                .filter(Room::isActive)
                .filter(room -> roomIds.stream().noneMatch(id -> id.equals(room.getId())))
                .toList()
                : Collections.emptyList();

        return UserDashboardResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .username(user.getUsername())
                .role(appRole)
                .gender(user.getGender())
                .permissions(permissions)
                .ownedRoomIds(ownedRoomIds)
                .memberRoomIds(memberRoomIds)
                .memberPublicRoomIds(memberPublicRoomIds)
                .memberPrivateRoomIds(memberPrivateRoomIds)
                .joinRequestRoomIdsSent(joinRequestRoomIdsSent)
                .chatRooms(chatRooms)
                .stats(DashboardStatsResponse.builder()
                        .roomCount(myRooms.size())
                        .ownedRoomCount(myRooms.stream().filter(room -> room.getOwner().getId().equals(user.getId())).count())
                        .upcomingLessonCount(upcomingLessons.size())
                        .liveSessionCount(liveSessions.size())
                        .unreadNotificationCount(unreadCount)
                        .pendingJoinRequestCount(canModerateRoomRequests
                                ? moderatedRequests.size()
                                : myJoinRequests.stream().filter(r -> r.getStatus() == RoomJoinRequestStatus.PENDING).count())
                        .recentMaterialCount(recentMaterials.size())
                        .build())
                .myRooms(memberships.stream()
                        .map(member -> toRoomCard(member.getRoom(), member, liveSessions))
                        .distinct()
                        .toList())
                .discoverRooms(discoverRooms.stream().map(room -> toRoomCard(room, null, liveSessions)).toList())
                .upcomingLessons(upcomingLessons.stream().limit(10).map(this::toLessonCard).toList())
                .liveSessions(liveSessions.stream().map(this::toLiveSessionCard).toList())
                .recentMaterials(recentMaterials.stream().map(this::toMaterialCard).toList())
                .notifications(notifications.stream().map(this::toNotificationCard).toList())
                .pendingJoinRequests(canModerateRoomRequests ? moderatedRequests.stream().map(this::toJoinRequestCard).toList() : Collections.emptyList())
                .myJoinRequests(myJoinRequests.stream().limit(10).map(this::toJoinRequestCard).toList())
                .systemSummary(null)
                .build();
    }

    public User update(User userY, UserDto dto) {
        User user = userRepository.findByEmail(userY.getEmail());
        if (user == null) {
            throw new NotFoundException("User not found for update");
        }

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setAddress(dto.getAddress());
        user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userRepository.save(user);
    }

    public void deleteMyAccount(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setIsDeleted(true);
        user.setEnabled(false);
        userRepository.save(user);
    }

    private UserDashboardResponse buildSuperAdminDashboard(User user, Set<AppPermissions> permissions) {
        List<Room> allRooms = roomRepository.findAll();
        List<LiveSession> liveSessions = liveSessionRepository.findAllByActiveTrue();
        List<LessonSchedule> upcomingLessons = lessonScheduleRepository.findTop10ByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());
        List<LibraryMaterial> recentMaterials = libraryMaterialRepository.findTop10ByActiveTrueOrderByDatetimeCreatedDesc();
        List<RoomJoinRequest> pendingRequests = roomJoinRequestRepository.findAllByStatusOrderByDatetimeCreatedAsc(RoomJoinRequestStatus.PENDING);
        List<Notification> notifications = notificationRepository.findTop10ByOrderByIdDesc();
        List<Long> ownedRoomIds = roomRepository.findByOwnerId(user.getId()).stream()
                .filter(Room::isActive)
                .map(Room::getId)
                .distinct()
                .sorted()
                .toList();
        List<com.example.onlinetalaba.dto.chatroom.ChatRoomMeResponse> chatRooms =
                chatRoomService.listMyRoomsForDashboard(user);

        return UserDashboardResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .username(user.getUsername())
                .role(AppRoleName.SUPER_ADMIN)
                .gender(user.getGender())
                .permissions(permissions)
                .ownedRoomIds(ownedRoomIds)
                .memberRoomIds(Collections.emptyList())
                .memberPublicRoomIds(Collections.emptyList())
                .memberPrivateRoomIds(Collections.emptyList())
                .joinRequestRoomIdsSent(Collections.emptyList())
                .chatRooms(chatRooms)
                .stats(DashboardStatsResponse.builder()
                        .roomCount(allRooms.size())
                        .ownedRoomCount(roomRepository.findByOwnerId(user.getId()).size())
                        .upcomingLessonCount(upcomingLessons.size())
                        .liveSessionCount(liveSessions.size())
                        .unreadNotificationCount(notificationRepository.countByUserIdAndIsReadFalse(user.getId()))
                        .pendingJoinRequestCount(pendingRequests.size())
                        .recentMaterialCount(recentMaterials.size())
                        .build())
                .myRooms(allRooms.stream().map(room -> toRoomCard(room, null, liveSessions)).toList())
                .discoverRooms(Collections.emptyList())
                .upcomingLessons(upcomingLessons.stream().map(this::toLessonCard).toList())
                .liveSessions(liveSessions.stream().map(this::toLiveSessionCard).toList())
                .recentMaterials(recentMaterials.stream().map(this::toMaterialCard).toList())
                .notifications(notifications.stream().map(this::toNotificationCard).toList())
                .pendingJoinRequests(pendingRequests.stream().map(this::toJoinRequestCard).toList())
                .myJoinRequests(Collections.emptyList())
                .systemSummary(DashboardSystemSummaryResponse.builder()
                        .totalUsers(userRepository.countByIsDeletedFalse())
                        .totalRooms(roomRepository.count())
                        .totalPrivateRooms(roomRepository.countByVisibilityAndActiveTrue(RoomVisibility.PRIVATE))
                        .totalPublicRooms(roomRepository.countByVisibilityAndActiveTrue(RoomVisibility.PUBLIC))
                        .totalLiveSessions(liveSessionRepository.countByActiveTrue())
                        .totalScheduledLessons(lessonScheduleRepository.countByStatus(LessonStatus.SCHEDULED))
                        .totalPendingJoinRequests(roomJoinRequestRepository.countByStatus(RoomJoinRequestStatus.PENDING))
                        .build())
                .build();
    }

    private DashboardRoomResponse toRoomCard(Room room, RoomMember membership, List<LiveSession> liveSessions) {
        boolean liveNow = liveSessions.stream().anyMatch(session -> session.getLessonSchedule().getRoom().getId().equals(room.getId()));
        RoomMemberRole role = membership == null ? null : membership.getRole();
        return DashboardRoomResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .subject(room.getSubject())
                .description(room.getDescription())
                .visibility(room.getVisibility())
                .active(room.isActive())
                .ownerId(room.getOwner().getId())
                .ownerName(room.getOwner().getFullName())
                .roomRole(role)
                .liveNow(liveNow)
                .canManageRoom(membership != null && (membership.getRole() == RoomMemberRole.OWNER || membership.isCanManageRoom()))
                .canInviteMembers(membership != null && (membership.getRole() == RoomMemberRole.OWNER || membership.isCanInviteMembers()))
                .canScheduleLesson(membership != null && (membership.getRole() == RoomMemberRole.OWNER || membership.isCanScheduleLesson()))
                .canUploadMaterials(membership != null && (membership.getRole() == RoomMemberRole.OWNER || membership.isCanUploadMaterials()))
                .build();
    }

    private DashboardLessonResponse toLessonCard(LessonSchedule lesson) {
        return DashboardLessonResponse.builder()
                .lessonId(lesson.getId())
                .roomId(lesson.getRoom().getId())
                .roomTitle(lesson.getRoom().getTitle())
                .title(lesson.getTitle())
                .teacherName(lesson.getTeacher().getFullName())
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .status(lesson.getStatus())
                .build();
    }

    private DashboardLiveSessionResponse toLiveSessionCard(LiveSession session) {
        return DashboardLiveSessionResponse.builder()
                .liveSessionId(session.getId())
                .lessonId(session.getLessonSchedule().getId())
                .roomId(session.getLessonSchedule().getRoom().getId())
                .roomTitle(session.getLessonSchedule().getRoom().getTitle())
                .lessonTitle(session.getLessonSchedule().getTitle())
                .hostName(session.getHost().getFullName())
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .build();
    }

    private DashboardMaterialResponse toMaterialCard(LibraryMaterial material) {
        String fileUrl = null;

        if (material.getAttachmentIds() != null && !material.getAttachmentIds().isEmpty()) {
            fileUrl = attachmentRepository.findById(material.getAttachmentIds().get(0))
                    .map(Attachment::getFileUrl)
                    .orElse(null);
        }

        return DashboardMaterialResponse.builder()
                .materialId(material.getId())
                .roomId(material.getRoom().getId())
                .roomTitle(material.getRoom().getTitle())
                .title(material.getTitle())
                .uploadedBy(material.getUploadedBy().getFullName())
                .materialType(material.getMaterialType())
                .fileUrl(fileUrl)
                .createdAt(material.getDatetimeCreated())
                .build();
    }

    private DashboardNotificationResponse toNotificationCard(Notification notification) {
        return DashboardNotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.getIsRead())
                .build();
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private UserSearchItemResponse mapUserSearchRow(Object[] row) {
        return UserSearchItemResponse.builder()
                .id(((Number) row[0]).longValue())
                .fullName(row[1] != null ? row[1].toString() : null)
                .username(row[2] != null ? row[2].toString() : null)
                .email(row[3] != null ? row[3].toString() : null)
                .phoneNumber(row[4] != null ? row[4].toString() : null)
                .address(row[5] != null ? row[5].toString() : null)
                .build();
    }

    private DashboardJoinRequestResponse toJoinRequestCard(RoomJoinRequest joinRequest) {
        return DashboardJoinRequestResponse.builder()
                .id(joinRequest.getId())
                .roomId(joinRequest.getRoom().getId())
                .roomTitle(joinRequest.getRoom().getTitle())
                .requesterId(joinRequest.getRequester().getId())
                .requesterName(joinRequest.getRequester().getFullName())
                .requesterEmail(joinRequest.getRequester().getEmail())
                .status(joinRequest.getStatus())
                .message(joinRequest.getMessage())
                .createdAt(joinRequest.getDatetimeCreated())
                .processedAt(joinRequest.getProcessedAt())
                .build();
    }
}
