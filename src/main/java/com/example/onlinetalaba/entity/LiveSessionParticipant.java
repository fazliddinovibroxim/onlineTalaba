package com.example.onlinetalaba.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "live_session_participants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_live_session_participant", columnNames = {"live_session_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_lsp_live_session", columnList = "live_session_id"),
                @Index(name = "idx_lsp_user", columnList = "user_id"),
                @Index(name = "idx_lsp_last_seen", columnList = "last_seen_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveSessionParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "live_session_id", nullable = false)
    private LiveSession liveSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "is_hand_raised", nullable = false)
    private boolean handRaised;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (joinedAt == null) {
            joinedAt = now;
        }
        if (lastSeenAt == null) {
            lastSeenAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (lastSeenAt == null) {
            lastSeenAt = LocalDateTime.now();
        }
    }
}

