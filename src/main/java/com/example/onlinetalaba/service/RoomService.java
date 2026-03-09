package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.room.RoomInviteRequest;
import com.example.onlinetalaba.dto.room.RoomRequest;
import com.example.onlinetalaba.dto.room.RoomResponse;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import com.example.onlinetalaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;

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

        return mapToResponse(room);
    }

    @Transactional(readOnly = true)
    public RoomResponse getById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return mapToResponse(room);
    }

    @Transactional
    public RoomResponse update(Long roomId, RoomRequest request, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (!(member.getRole() == RoomMemberRole.OWNER || member.isCanManageRoom())) {
            throw new RuntimeException("You do not have permission to update this room");
        }

        room.setTitle(request.getTitle());
        room.setDescription(request.getDescription());
        room.setSubject(request.getSubject());
        room.setVisibility(request.getVisibility());

        roomRepository.save(room);
        return mapToResponse(room);
    }

    @Transactional
    public void delete(Long roomId, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only owner can delete room");
        }

        room.setActive(false);
        roomRepository.save(room);
    }

    @Transactional
    public void inviteMember(Long roomId, RoomInviteRequest request, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        RoomMember inviter = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (!(inviter.getRole() == RoomMemberRole.OWNER || inviter.isCanInviteMembers())) {
            throw new RuntimeException("You do not have permission to invite members");
        }

        if (roomMemberRepository.existsByRoomIdAndUserId(roomId, request.getUserId())) {
            throw new RuntimeException("User already exists in this room");
        }

        User invitedUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

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

    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .description(room.getDescription())
                .subject(room.getSubject())
                .visibility(room.getVisibility())
                .ownerId(room.getOwner().getId())
                .ownerName(room.getOwner().getFullName())
                .build();
    }
}