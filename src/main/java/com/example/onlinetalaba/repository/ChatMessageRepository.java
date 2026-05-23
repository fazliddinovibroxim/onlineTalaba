package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findAllByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    Optional<ChatMessage> findByIdAndChatRoomId(Long id, Long chatRoomId);

    long countByChatRoomIdAndSenderIdNotAndIsReadFalse(Long chatRoomId, Long senderId);

    @Modifying
    @Query("""
            UPDATE ChatMessage m
            SET m.isRead = true
            WHERE m.chatRoom.id = :chatRoomId
              AND m.sender.id <> :readerId
              AND m.isRead = false
            """)
    int markAllAsRead(@Param("chatRoomId") Long chatRoomId, @Param("readerId") Long readerId);

    @Modifying
    @Query("""
            UPDATE ChatMessage m
            SET m.isRead = true
            WHERE m.id = :messageId
              AND m.chatRoom.id = :chatRoomId
              AND m.sender.id <> :readerId
            """)
    int markAsRead(
            @Param("chatRoomId") Long chatRoomId,
            @Param("messageId") Long messageId,
            @Param("readerId") Long readerId
    );
}
