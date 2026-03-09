package com.example.onlinetalaba.entity;

import com.example.onlinetalaba.enums.ChatMessageType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room_chat_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatMessageType messageType;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = false)
    private boolean edited = false;

    @Column(nullable = false)
    private boolean deleted = false;
}