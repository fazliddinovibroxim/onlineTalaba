package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.chatroom.ChatMessageRequest;
import com.example.onlinetalaba.dto.chatroom.ChatMessageResponse;
import com.example.onlinetalaba.dto.chatroom.ChatMessageUpdateRequest;
import com.example.onlinetalaba.entity.ChatMessage;
import com.example.onlinetalaba.entity.ChatRoom;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.handler.BadRequestException;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final int MAX_MESSAGE_LENGTH = 5000;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> list(Long chatRoomId, User currentUser) {
        chatRoomService.getRoomForUser(chatRoomId, currentUser);
        return chatMessageRepository.findAllByChatRoomIdOrderByCreatedAtAsc(chatRoomId)
                .stream()
                .map(this::mapMessage)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatMessageResponse getById(Long chatRoomId, Long messageId, User currentUser) {
        chatRoomService.getRoomForUser(chatRoomId, currentUser);
        ChatMessage message = chatMessageRepository.findByIdAndChatRoomId(messageId, chatRoomId)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        return mapMessage(message);
    }

    @Transactional
    public ChatMessageResponse create(Long chatRoomId, ChatMessageRequest request, User currentUser) {
        ChatRoom chatRoom = chatRoomService.getRoomForUser(chatRoomId, currentUser);
        String text = normalizeMessage(request != null ? request.getMessage() : null);

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(currentUser)
                .message(text)
                .attachmentUrl(request != null ? trimToNull(request.getAttachmentUrl()) : null)
                .attachmentName(request != null ? trimToNull(request.getAttachmentName()) : null)
                .isRead(false)
                .build();

        chatMessageRepository.save(message);
        chatRoomService.touchRoom(chatRoom, text);

        return mapMessage(message);
    }

    @Transactional
    public ChatMessageResponse update(Long chatRoomId, Long messageId, ChatMessageUpdateRequest request, User currentUser) {
        chatRoomService.getRoomForUser(chatRoomId, currentUser);

        ChatMessage message = chatMessageRepository.findByIdAndChatRoomId(messageId, chatRoomId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only edit your own messages");
        }

        if (request != null && request.getMessage() != null) {
            message.setMessage(normalizeMessage(request.getMessage()));
        }
        if (request != null) {
            if (request.getAttachmentUrl() != null) {
                message.setAttachmentUrl(trimToNull(request.getAttachmentUrl()));
            }
            if (request.getAttachmentName() != null) {
                message.setAttachmentName(trimToNull(request.getAttachmentName()));
            }
        }

        chatMessageRepository.save(message);
        return mapMessage(message);
    }

    @Transactional
    public void delete(Long chatRoomId, Long messageId, User currentUser) {
        chatRoomService.getRoomForUser(chatRoomId, currentUser);

        ChatMessage message = chatMessageRepository.findByIdAndChatRoomId(messageId, chatRoomId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own messages");
        }

        chatMessageRepository.delete(message);
    }

    @Transactional
    public int markAllAsRead(Long chatRoomId, User currentUser) {
        chatRoomService.getRoomForUser(chatRoomId, currentUser);
        return chatMessageRepository.markAllAsRead(chatRoomId, currentUser.getId());
    }

    @Transactional
    public void markAsRead(Long chatRoomId, Long messageId, User currentUser) {
        chatRoomService.getRoomForUser(chatRoomId, currentUser);
        int updated = chatMessageRepository.markAsRead(chatRoomId, messageId, currentUser.getId());
        if (updated == 0) {
            ChatMessage message = chatMessageRepository.findByIdAndChatRoomId(messageId, chatRoomId)
                    .orElseThrow(() -> new NotFoundException("Message not found"));
            if (message.getSender().getId().equals(currentUser.getId())) {
                return;
            }
            throw new NotFoundException("Message not found or already read");
        }
    }

    private ChatMessageResponse mapMessage(ChatMessage message) {
        User sender = message.getSender();
        String displayName = sender.getFullName() != null && !sender.getFullName().isBlank()
                ? sender.getFullName()
                : sender.getUsername();

        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .senderId(sender.getId())
                .senderName(displayName)
                .senderUsername(sender.getUsername())
                .message(message.getMessage())
                .attachmentUrl(message.getAttachmentUrl())
                .attachmentName(message.getAttachmentName())
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new BadRequestException("Message text is required");
        }
        String trimmed = message.trim();
        if (trimmed.length() > MAX_MESSAGE_LENGTH) {
            throw new BadRequestException("Message text is too long");
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
