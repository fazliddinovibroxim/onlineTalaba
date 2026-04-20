package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.room.RoomJoinRequestDecisionRequest;
import com.example.onlinetalaba.dto.room.RoomJoinRequestRequest;
import com.example.onlinetalaba.dto.room.RoomJoinRequestResponse;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.entity.RoomJoinRequest;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.RoomJoinRequestStatus;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.handler.ConflictException;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.notification.NotificationService;
import com.example.onlinetalaba.repository.RoomJoinRequestRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomJoinRequestService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomJoinRequestRepository roomJoinRequestRepository;
    private final NotificationService notificationService;

    @Transactional
    public RoomJoinRequestResponse create(Long roomId, RoomJoinRequestRequest request, User currentUser) {
        Room room = getActiveRoom(roomId);

        if (roomMemberRepository.existsByRoomIdAndUserId(roomId, currentUser.getId())) {
            throw new ConflictException("User already exists in this room");
        }

        if (roomJoinRequestRepository.existsByRoomIdAndRequesterIdAndStatus(
                roomId,
                currentUser.getId(),
                RoomJoinRequestStatus.PENDING
        )) {
            throw new ConflictException("Join request already pending");
        }

        RoomJoinRequest joinRequest = RoomJoinRequest.builder()
                .room(room)
                .requester(currentUser)
                .message(request == null ? null : request.getMessage())
                .status(RoomJoinRequestStatus.PENDING)
                .build();

        roomJoinRequestRepository.save(joinRequest);

        roomMemberRepository.findAllByRoomIdAndActiveTrue(roomId).stream()
                .filter(member -> member.getRole() == RoomMemberRole.OWNER || member.getRole() == RoomMemberRole.TEACHER)
                .map(RoomMember::getUser)
                .forEach(user -> notificationService.createAndDispatch(
                        user,
                        "New room join request",
                        currentUser.getFullName() + " " + room.getTitle() + " xonasiga qo‘shilish so‘rovi yubordi"
                ));

        return toResponse(joinRequest);
    }

    @Transactional(readOnly = true)
    public List<RoomJoinRequestResponse> getPendingRequests(Long roomId, User currentUser) {
        Room room = getActiveRoom(roomId);
        ensureCanModerateRequests(room, currentUser);

        return roomJoinRequestRepository.findAllByRoomIdAndStatusOrderByDatetimeCreatedAsc(roomId, RoomJoinRequestStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RoomJoinRequestResponse approve(Long roomId,
                                           Long joinRequestId,
                                           RoomJoinRequestDecisionRequest request,
                                           User currentUser) {
        Room room = getActiveRoom(roomId);
        ensureCanModerateRequests(room, currentUser);

        RoomJoinRequest joinRequest = getPendingRequest(roomId, joinRequestId);

        if (roomMemberRepository.existsByRoomIdAndUserId(roomId, joinRequest.getRequester().getId())) {
            throw new ConflictException("User already exists in this room");
        }

        User requester = joinRequest.getRequester();
        AppRoleName appRole = requester.getRoles().getAppRoleName();
        RoomMemberRole roomRole = (appRole == AppRoleName.TEACHER) ? RoomMemberRole.TEACHER : RoomMemberRole.STUDENT;
        boolean isTeacher = (roomRole == RoomMemberRole.TEACHER);

        roomMemberRepository.save(RoomMember.builder()
                .room(room)
                .user(requester)
                .role(roomRole)
                .canManageRoom(false)
                .canInviteMembers(false)
                .canScheduleLesson(isTeacher)
                .canUploadMaterials(isTeacher)
                .active(true)
                .build());

        joinRequest.setStatus(RoomJoinRequestStatus.APPROVED);
        joinRequest.setProcessedAt(LocalDateTime.now());
        joinRequest.setProcessedBy(currentUser);
        if (request != null && request.getMessage() != null && !request.getMessage().isBlank()) {
            joinRequest.setMessage(request.getMessage().trim());
        }
        roomJoinRequestRepository.save(joinRequest);

        notificationService.createAndDispatch(
                joinRequest.getRequester(),
                "Room request approved",
                room.getTitle() + " xonasiga qo‘shilish so‘rovingiz tasdiqlandi"
        );

        return toResponse(joinRequest);
    }

    @Transactional
    public RoomJoinRequestResponse reject(Long roomId,
                                          Long joinRequestId,
                                          RoomJoinRequestDecisionRequest request,
                                          User currentUser) {
        Room room = getActiveRoom(roomId);
        ensureCanModerateRequests(room, currentUser);

        RoomJoinRequest joinRequest = getPendingRequest(roomId, joinRequestId);

        joinRequest.setStatus(RoomJoinRequestStatus.REJECTED);
        joinRequest.setProcessedAt(LocalDateTime.now());
        joinRequest.setProcessedBy(currentUser);
        if (request != null && request.getMessage() != null && !request.getMessage().isBlank()) {
            joinRequest.setMessage(request.getMessage().trim());
        }
        roomJoinRequestRepository.save(joinRequest);

        notificationService.createAndDispatch(
                joinRequest.getRequester(),
                "Room request rejected",
                room.getTitle() + " xonasiga qo‘shilish so‘rovingiz rad etildi"
        );

        return toResponse(joinRequest);
    }

    private RoomJoinRequest getPendingRequest(Long roomId, Long joinRequestId) {
        RoomJoinRequest joinRequest = roomJoinRequestRepository.findByIdAndRoomId(joinRequestId, roomId)
                .orElseThrow(() -> new NotFoundException("Join request not found"));

        if (joinRequest.getStatus() != RoomJoinRequestStatus.PENDING) {
            throw new ConflictException("Join request already processed");
        }

        return joinRequest;
    }

    private Room getActiveRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        if (!room.isActive()) {
            throw new ForbiddenException("Room is not active");
        }

        return room;
    }

    private void ensureCanModerateRequests(Room room, User currentUser) {
        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(room.getId(), currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        boolean canModerate = member.getRole() == RoomMemberRole.OWNER
                || member.getRole() == RoomMemberRole.TEACHER;

        if (!canModerate) {
            throw new ForbiddenException("Only room owner or teacher can process join requests");
        }
    }

    private RoomJoinRequestResponse toResponse(RoomJoinRequest joinRequest) {
        return RoomJoinRequestResponse.builder()
                .id(joinRequest.getId())
                .roomId(joinRequest.getRoom().getId())
                .requesterId(joinRequest.getRequester().getId())
                .requesterName(joinRequest.getRequester().getFullName())
                .requesterEmail(joinRequest.getRequester().getEmail())
                .message(joinRequest.getMessage())
                .status(joinRequest.getStatus())
                .processedById(joinRequest.getProcessedBy() == null ? null : joinRequest.getProcessedBy().getId())
                .processedByName(joinRequest.getProcessedBy() == null ? null : joinRequest.getProcessedBy().getFullName())
                .createdAt(joinRequest.getDatetimeCreated())
                .processedAt(joinRequest.getProcessedAt())
                .build();
    }
}
