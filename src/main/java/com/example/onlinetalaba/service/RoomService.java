package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.room.RoomInviteRequest;
import com.example.onlinetalaba.dto.room.RoomMemberUserResponse;
import com.example.onlinetalaba.dto.room.RoomRequest;
import com.example.onlinetalaba.dto.room.RoomResponse;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.RoomJoinRequestStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.enums.RoomVisibility;
import com.example.onlinetalaba.handler.ConflictException;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LiveSessionRepository;
import com.example.onlinetalaba.repository.RoomJoinRequestRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import com.example.onlinetalaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final RoomJoinRequestRepository roomJoinRequestRepository;

    @Transactional
    public RoomResponse create(RoomRequest request, User currentUser) {
        Room room = Room.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .subject(request.getSubject())
                .visibility(request.getVisibility())
                .owner(currentUser)
                .active(true)
                .build();

        roomRepository.save(room);

        RoomMember ownerMember = RoomMember.builder()
                .room(room)
                .user(currentUser)
                .role(RoomMemberRole.OWNER)
                .canManageRoom(true)
                .canInviteMembers(true)
                .canScheduleLesson(true)
                .canUploadMaterials(true)
                .active(true)
                .build();

        roomMemberRepository.save(ownerMember);

        addInitialMembers(room, request, currentUser);
        return mapToResponse(room, currentUser, true);
    }

    @Transactional(readOnly = true)
    public RoomResponse getById(Long roomId, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        // Everyone can see basic room info now
        return mapToResponse(room, currentUser, true);
    }

    @Transactional
    public RoomResponse update(Long roomId, RoomRequest request, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (!room.isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        if (!(member.getRole() == RoomMemberRole.OWNER || member.isCanManageRoom())) {
            throw new ForbiddenException("You do not have permission to update this room");
        }

        room.setTitle(request.getTitle());
        room.setDescription(request.getDescription());
        room.setSubject(request.getSubject());
        room.setVisibility(request.getVisibility());

        roomRepository.save(room);
        return mapToResponse(room, currentUser, true);
    }

    @Transactional
    public void delete(Long roomId, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        if (!room.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only owner can delete room");
        }

        room.setActive(false);
        roomRepository.save(room);
    }

    @Transactional
    public void inviteMember(Long roomId, RoomInviteRequest request, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        RoomMember inviter = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (!room.isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        if (!(inviter.getRole() == RoomMemberRole.OWNER || inviter.isCanInviteMembers())) {
            throw new ForbiddenException("You do not have permission to invite members");
        }

        if (roomMemberRepository.existsByRoomIdAndUserId(roomId, request.getUserId())) {
            throw new ConflictException("User already exists in this room");
        }

        User invitedUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        RoomMemberRole finalRole = request.getRole();
        if (finalRole == null) {
            AppRoleName appRole = invitedUser.getRoles().getAppRoleName();
            finalRole = (appRole == AppRoleName.TEACHER) ? RoomMemberRole.TEACHER : RoomMemberRole.STUDENT;
        }
        boolean isTeacher = finalRole == RoomMemberRole.TEACHER;

        RoomMember member = RoomMember.builder()
                .room(room)
                .user(invitedUser)
                .role(finalRole)
                .canManageRoom(request.isCanManageRoom())
                .canInviteMembers(request.isCanInviteMembers())
                .canScheduleLesson(request.isCanScheduleLesson() || isTeacher)
                .canUploadMaterials(request.isCanUploadMaterials() || isTeacher)
                .active(true)
                .build();

        roomMemberRepository.save(member);
    }

    @Transactional
    public RoomResponse join(Long roomId, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        if (!room.isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        if (roomMemberRepository.existsByRoomIdAndUserId(roomId, currentUser.getId())) {
            return mapToResponse(room, currentUser, true);
        }

        if (room.getVisibility() != RoomVisibility.PUBLIC) {
            throw new ForbiddenException("You cannot join a private room without invitation");
        }

        AppRoleName appRole = currentUser.getRoles().getAppRoleName();
        RoomMemberRole roomRole = (appRole == AppRoleName.TEACHER) ? RoomMemberRole.TEACHER : RoomMemberRole.STUDENT;
        boolean isTeacher = (roomRole == RoomMemberRole.TEACHER);

        RoomMember member = RoomMember.builder()
                .room(room)
                .user(currentUser)
                .role(roomRole)
                .canManageRoom(false)
                .canInviteMembers(false)
                .canScheduleLesson(isTeacher)
                .canUploadMaterials(isTeacher)
                .active(true)
                .build();

        roomMemberRepository.save(member);
        return mapToResponse(room, currentUser, true);
    }

    @Transactional(readOnly = true)
    public Page<RoomResponse> search(String q,
                                    RoomVisibility visibility,
                                    Long ownerId,
                                    boolean includeMembers,
                                    Pageable pageable,
                                    User currentUser) {
        Specification<Room> spec = (root, query, cb) -> cb.isTrue(root.get("active"));

        if (visibility != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("visibility"), visibility));
        }
        if (ownerId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("owner").get("id"), ownerId));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                var owner = root.join("owner");
                return cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("subject")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like),
                        cb.like(cb.lower(cb.coalesce(owner.get("fullName"), "")), like),
                        cb.like(cb.lower(cb.coalesce(owner.get("username"), "")), like)
                );
            });
        }

        return roomRepository.findAll(spec, pageable)
                .map(room -> mapToResponse(room, currentUser, includeMembers));
    }

    private RoomResponse mapToResponse(Room room, User currentUser, boolean includeMembers) {
        Optional<RoomMember> myMembership = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(room.getId(), currentUser.getId());
        RoomMember member = myMembership.orElse(null);

        boolean canModerateJoinRequests = member != null
                && (member.getRole() == RoomMemberRole.OWNER || member.getRole() == RoomMemberRole.TEACHER);

        List<RoomMemberUserResponse> members = null;
        boolean canSeeMembers = includeMembers
                && (room.getVisibility() == RoomVisibility.PUBLIC
                || member != null
                || currentUser.getRoles().getAppRoleName() == AppRoleName.SUPER_ADMIN
                || currentUser.getRoles().getAppRoleName() == AppRoleName.ADMIN);

        if (canSeeMembers) {
            members = roomMemberRepository.findAllByRoomIdAndActiveTrue(room.getId()).stream()
                    .filter(m -> m.getUser() != null)
                    .map(m -> RoomMemberUserResponse.builder()
                            .userId(m.getUser().getId())
                            .fullName(m.getUser().getFullName())
                            .username(m.getUser().getUsername())
                            .email(m.getUser().getEmail())
                            .roomRole(m.getRole())
                            .canManageRoom(m.getRole() == RoomMemberRole.OWNER || m.isCanManageRoom())
                            .canInviteMembers(m.getRole() == RoomMemberRole.OWNER || m.isCanInviteMembers())
                            .canScheduleLesson(m.getRole() == RoomMemberRole.OWNER || m.isCanScheduleLesson())
                            .canUploadMaterials(m.getRole() == RoomMemberRole.OWNER || m.isCanUploadMaterials())
                            .active(m.isActive())
                            .build())
                    .sorted(Comparator.comparing(RoomMemberUserResponse::getUserId))
                    .toList();
        }

        return RoomResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .description(room.getDescription())
                .subject(room.getSubject())
                .visibility(room.getVisibility())
                .ownerId(room.getOwner().getId())
                .ownerName(room.getOwner().getFullName())
                .active(room.isActive())
                .memberCount(roomMemberRepository.countByRoomIdAndActiveTrue(room.getId()))
                .liveNow(liveSessionRepository.existsByLessonScheduleRoomIdAndActiveTrue(room.getId()))
                .pendingJoinRequestCount(canModerateJoinRequests
                        ? roomJoinRequestRepository.countByRoomIdAndStatus(room.getId(), RoomJoinRequestStatus.PENDING)
                        : 0)
                .createdAt(room.getDatetimeCreated())
                .updatedAt(room.getDatetimeUpdated())
                .member(member != null)
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
                .members(members)
                .build();
    }

    private void addInitialMembers(Room room, RoomRequest request, User currentUser) {
        if (request == null || request.getMembers() == null || request.getMembers().isEmpty()) {
            return;
        }

        for (RoomInviteRequest invite : request.getMembers()) {
            if (invite == null || invite.getUserId() == null) {
                continue;
            }
            if (Objects.equals(invite.getUserId(), currentUser.getId())) {
                continue; // owner already added
            }
            if (roomMemberRepository.existsByRoomIdAndUserId(room.getId(), invite.getUserId())) {
                continue;
            }

            User invitedUser = userRepository.findById(invite.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found"));
            if (Boolean.TRUE.equals(invitedUser.getIsDeleted())) {
                continue;
            }

            RoomMemberRole finalRole = invite.getRole();
            if (finalRole == null) {
                AppRoleName appRole = invitedUser.getRoles().getAppRoleName();
                finalRole = (appRole == AppRoleName.TEACHER) ? RoomMemberRole.TEACHER : RoomMemberRole.STUDENT;
            }
            boolean isTeacher = finalRole == RoomMemberRole.TEACHER;

            roomMemberRepository.save(RoomMember.builder()
                    .room(room)
                    .user(invitedUser)
                    .role(finalRole)
                    .canManageRoom(invite.isCanManageRoom())
                    .canInviteMembers(invite.isCanInviteMembers())
                    .canScheduleLesson(invite.isCanScheduleLesson() || isTeacher)
                    .canUploadMaterials(invite.isCanUploadMaterials() || isTeacher)
                    .active(true)
                    .build());
        }
    }

    public boolean hasFullAccess(Room room, User currentUser) {
        AppRoleName role = currentUser.getRoles().getAppRoleName();
        if (role == AppRoleName.SUPER_ADMIN || role == AppRoleName.ADMIN) {
            return true;
        }

        if (room.getVisibility() == RoomVisibility.PUBLIC) {
            return true;
        }

        return roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(room.getId(), currentUser.getId()).isPresent();
    }

    public void validateFullAccess(Room room, User currentUser) {
        if (!hasFullAccess(room, currentUser)) {
            throw new ForbiddenException("You do not have access to this room. Please join first.");
        }
    }

    // Interaktiv feature'lar (chat/live/whiteboard/hand-raise) uchun: PUBLIC bo'lsa ham a'zolik talab qilinadi.
    public boolean hasMemberAccess(Room room, User currentUser) {
        AppRoleName role = currentUser.getRoles().getAppRoleName();
        if (role == AppRoleName.SUPER_ADMIN || role == AppRoleName.ADMIN) {
            return true;
        }

        return roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(room.getId(), currentUser.getId()).isPresent();
    }

    public void validateMemberAccess(Room room, User currentUser) {
        if (!hasMemberAccess(room, currentUser)) {
            throw new ForbiddenException("Please join this room first.");
        }
    }
}
