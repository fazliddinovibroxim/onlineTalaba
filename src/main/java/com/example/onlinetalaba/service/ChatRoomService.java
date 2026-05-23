package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.chatroom.ChatRoomMeResponse;
import com.example.onlinetalaba.dto.chatroom.ChatRoomResponse;
import com.example.onlinetalaba.dto.dashboard.UserSearchItemResponse;
import com.example.onlinetalaba.entity.ChatRoom;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.handler.BadRequestException;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.ChatMessageRepository;
import com.example.onlinetalaba.repository.ChatRoomRepository;
import com.example.onlinetalaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private static final int PREVIEW_MAX = 200;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoomResponse startOrGet(Long otherUserId, User currentUser) {
        User otherUser = resolveOtherUser(otherUserId, currentUser);
        ChatRoom chatRoom = getOrCreateRoom(currentUser, otherUser);
        return mapRoom(chatRoom, currentUser);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> listMyRooms(User currentUser) {
        return chatRoomRepository.findAllForUser(currentUser.getId())
                .stream()
                .map(room -> mapRoom(room, currentUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomMeResponse> listMyRoomsForDashboard(User currentUser) {
        return chatRoomRepository.findAllForUser(currentUser.getId())
                .stream()
                .map(room -> mapRoomForMe(room, currentUser))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getById(Long chatRoomId, User currentUser) {
        ChatRoom chatRoom = getRoomForUser(chatRoomId, currentUser);
        return mapRoom(chatRoom, currentUser);
    }

    @Transactional
    public void deleteRoom(Long chatRoomId, User currentUser) {
        ChatRoom chatRoom = getRoomForUser(chatRoomId, currentUser);
        chatRoomRepository.delete(chatRoom);
    }

    @Transactional(readOnly = true)
    public ChatRoom getRoomForUser(Long chatRoomId, User currentUser) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NotFoundException("Chat room not found"));

        if (!isParticipant(chatRoom, currentUser.getId())) {
            throw new ForbiddenException("Access denied");
        }
        return chatRoom;
    }

    @Transactional
    public void touchRoom(ChatRoom chatRoom, String messageText) {
        chatRoom.setLastMessageText(preview(messageText));
        chatRoom.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);
    }

    private ChatRoom getOrCreateRoom(User currentUser, User otherUser) {
        long userOneId = Math.min(currentUser.getId(), otherUser.getId());
        long userTwoId = Math.max(currentUser.getId(), otherUser.getId());

        return chatRoomRepository.findByUserOneIdAndUserTwoId(userOneId, userTwoId)
                .orElseGet(() -> {
                    User userOne = userOneId == currentUser.getId() ? currentUser : otherUser;
                    User userTwo = userTwoId == currentUser.getId() ? currentUser : otherUser;

                    ChatRoom created = ChatRoom.builder()
                            .userOne(userOne)
                            .userTwo(userTwo)
                            .lastMessageAt(LocalDateTime.now())
                            .build();
                    return chatRoomRepository.save(created);
                });
    }

    private User resolveOtherUser(Long otherUserId, User currentUser) {
        if (otherUserId == null) {
            throw new BadRequestException("otherUserId is required");
        }
        if (otherUserId.equals(currentUser.getId())) {
            throw new BadRequestException("You cannot start a chat with yourself");
        }

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (Boolean.TRUE.equals(otherUser.getIsDeleted())) {
            throw new NotFoundException("User not found");
        }
        if (!otherUser.isEnabled()) {
            throw new ForbiddenException("User is not available for chat");
        }
        return otherUser;
    }

    private boolean isParticipant(ChatRoom chatRoom, Long userId) {
        return chatRoom.getUserOne().getId().equals(userId)
                || chatRoom.getUserTwo().getId().equals(userId);
    }

    private ChatRoomResponse mapRoom(ChatRoom chatRoom, User currentUser) {
        User other = resolveOtherUserInRoom(chatRoom, currentUser);
        long unread = chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(
                chatRoom.getId(),
                currentUser.getId()
        );

        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .otherUser(toUserItem(other))
                .lastMessageText(chatRoom.getLastMessageText())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .unreadCount(unread)
                .build();
    }

    private ChatRoomMeResponse mapRoomForMe(ChatRoom chatRoom, User currentUser) {
        User other = resolveOtherUserInRoom(chatRoom, currentUser);
        long unread = chatMessageRepository.countByChatRoomIdAndSenderIdNotAndIsReadFalse(
                chatRoom.getId(),
                currentUser.getId()
        );

        return ChatRoomMeResponse.builder()
                .chatRoomId(chatRoom.getId())
                .otherUser(toUserItem(other))
                .lastMessageText(chatRoom.getLastMessageText())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .unreadCount(unread)
                .build();
    }

    private User resolveOtherUserInRoom(ChatRoom chatRoom, User currentUser) {
        return chatRoom.getUserOne().getId().equals(currentUser.getId())
                ? chatRoom.getUserTwo()
                : chatRoom.getUserOne();
    }

    private UserSearchItemResponse toUserItem(User user) {
        return UserSearchItemResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .build();
    }

    private String preview(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String trimmed = content.trim();
        if (trimmed.length() <= PREVIEW_MAX) {
            return trimmed;
        }
        return trimmed.substring(0, PREVIEW_MAX) + "...";
    }
}
