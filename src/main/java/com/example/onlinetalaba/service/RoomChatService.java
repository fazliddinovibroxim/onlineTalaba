package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.chat.RoomChatMessageRequest;
import com.example.onlinetalaba.dto.chat.RoomChatMessageResponse;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.entity.RoomChatMessage;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.ChatMessageType;
import com.example.onlinetalaba.repository.RoomChatMessageRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomChatService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomChatMessageRepository roomChatMessageRepository;

    @Transactional
    public RoomChatMessageResponse send(RoomChatMessageRequest request, User currentUser) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        RoomMember member = roomMemberRepository
                .findByRoomIdAndUserIdAndActiveTrue(room.getId(), currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (!member.isActive()) {
            throw new RuntimeException("Inactive room member");
        }

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

    @Transactional(readOnly = true)
    public List<RoomChatMessageResponse> getRoomMessages(Long roomId, User currentUser) {
        roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

        return roomChatMessageRepository.findAllByRoomIdAndDeletedFalseOrderByIdAsc(roomId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void deleteMessage(Long messageId, User currentUser) {
        RoomChatMessage message = roomChatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(
                        message.getRoom().getId(), currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Access denied"));

        boolean canDelete = message.getSender().getId().equals(currentUser.getId())
                || member.isCanManageRoom()
                || member.isCanInviteMembers(); // xohlasangiz moderator permission alohida qilasiz

        if (!canDelete) {
            throw new RuntimeException("You cannot delete this message");
        }

        message.setDeleted(true);
        roomChatMessageRepository.save(message);
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