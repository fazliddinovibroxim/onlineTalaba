package com.example.onlinetalaba.entity;

import com.example.onlinetalaba.enums.HandRaiseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hand_raises")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandRaise extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "live_session_id")
    private LiveSession liveSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HandRaiseStatus status;

    @Column(nullable = false)
    private boolean active;

    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;
}