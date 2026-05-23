package com.example.onlinetalaba.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_rooms",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_user_one_user_two",
                        columnNames = {"user_one_id", "user_two_id"}
                )
        },
        indexes = {
                @Index(name = "idx_chat_room_user_one", columnList = "user_one_id"),
                @Index(name = "idx_chat_room_user_two", columnList = "user_two_id"),
                @Index(name = "idx_chat_room_last_message_at", columnList = "last_message_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseLongEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;

    @Column(name = "last_message_text", columnDefinition = "TEXT")
    private String lastMessageText;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
}
