package com.example.onlinetalaba.entity;

import com.example.onlinetalaba.enums.WhiteboardEventType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "whiteboard_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhiteboardEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "live_session_id")
    private LiveSession liveSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WhiteboardEventType eventType;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(nullable = false)
    private boolean deleted;
}