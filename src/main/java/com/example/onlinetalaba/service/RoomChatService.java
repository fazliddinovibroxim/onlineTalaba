package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.chat.RoomChatMessageRequest;
import com.example.onlinetalaba.dto.chat.RoomChatMessageResponse;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.entity.RoomChatMessage;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.ChatMessageType;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.RoomChatMessageRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomChatService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomChatMessageRepository roomChatMessageRepository;
    private final AttachmentService attachmentService;

    @Transactional
    public RoomChatMessageResponse send(RoomChatMessageRequest request, User currentUser) {
        Room room = getActiveRoomMemberRoom(request.getRoomId(), currentUser);

        RoomChatMessage message = RoomChatMessage.builder()
                .room(room)
                .sender(currentUser)
                .content(request.getContent())
                .messageType(request.getMessageType() == null ? ChatMessageType.TEXT : request.getMessageType())
                .edited(false)
                .deleted(false)
                .build();

        roomChatMessageRepository.save(message);

        return mapToResponse(message);
    }

    @Transactional
    public RoomChatMessageResponse sendToRoom(Long roomId, RoomChatMessageRequest request, User currentUser) {
        request.setRoomId(roomId);
        return send(request, currentUser);
    }

    @Transactional
    public RoomChatMessageResponse sendImage(Long roomId, MultipartFile file, User currentUser) throws java.io.IOException {
        Room room = getActiveRoomMemberRoom(roomId, currentUser);
        String imageUrl = attachmentService.uploadChatImage(file);

        RoomChatMessage message = RoomChatMessage.builder()
                .room(room)
                .sender(currentUser)
                .content(imageUrl)
                .messageType(ChatMessageType.FILE)
                .edited(false)
                .deleted(false)
                .build();

        roomChatMessageRepository.save(message);
        return mapToResponse(message);
    }

    @Transactional(readOnly = true)
    public List<RoomChatMessageResponse> getRoomMessages(Long roomId, User currentUser) {
        getActiveRoomMemberRoom(roomId, currentUser);

        return roomChatMessageRepository.findAllByRoomIdAndDeletedFalseOrderByIdAsc(roomId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void deleteMessage(Long messageId, User currentUser) {
        RoomChatMessage message = roomChatMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        message.getRoom().getId(), currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        boolean canDelete = message.getSender().getId().equals(currentUser.getId())
                || member.isCanManageRoom()
                || member.isCanInviteMembers(); // xohlasangiz moderator permission alohida qilasiz

        if (!canDelete) {
            throw new ForbiddenException("You cannot delete this message");
        }

        message.setDeleted(true);
        roomChatMessageRepository.save(message);
    }

    private Room getActiveRoomMemberRoom(Long roomId, User currentUser) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        RoomMember member = roomMemberRepository
                .findByRoomIdAndUserIdAndActiveTrue(room.getId(), currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (!member.isActive()) {
            throw new ForbiddenException("Inactive room member");
        }

        return room;
    }

    private RoomChatMessageResponse mapToResponse(RoomChatMessage message) {
        return RoomChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoom().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderEmail(message.getSender().getEmail())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .edited(message.isEdited())
                .createdAt(message.getDatetimeCreated())
                .build();
    }
}
