package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.room.RoomInviteRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

        return mapToResponse(room, currentUser);
    }

    @Transactional(readOnly = true)
    public RoomResponse getById(Long roomId, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));
        // Everyone can see basic room info now
        return mapToResponse(room, currentUser);
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
        return mapToResponse(room, currentUser);
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

        RoomMember member = RoomMember.builder()
                .room(room)
                .user(invitedUser)
                .role(request.getRole())
                .canManageRoom(request.isCanManageRoom())
                .canInviteMembers(request.isCanInviteMembers())
                .canScheduleLesson(request.isCanScheduleLesson())
                .canUploadMaterials(request.isCanUploadMaterials())
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
            return mapToResponse(room, currentUser);
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
        return mapToResponse(room, currentUser);
    }

    private RoomResponse mapToResponse(Room room, User currentUser) {
        Optional<RoomMember> myMembership = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(room.getId(), currentUser.getId());
        RoomMember member = myMembership.orElse(null);

        boolean canModerateJoinRequests = member != null
                && (member.getRole() == RoomMemberRole.OWNER || member.getRole() == RoomMemberRole.TEACHER);

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
                .build();
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
}
