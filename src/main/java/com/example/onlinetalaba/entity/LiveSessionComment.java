package com.example.onlinetalaba.entity;

import com.example.onlinetalaba.enums.LessonCommentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "live_session_comments",
        indexes = {
                @Index(name = "idx_lsc_live_session", columnList = "live_session_id"),
                @Index(name = "idx_lsc_sender", columnList = "sender_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveSessionComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "live_session_id", nullable = false)
    private LiveSession liveSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false)
    private LessonCommentType commentType;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = false)
    private boolean edited = false;

    @Column(nullable = false)
    private boolean deleted = false;
}
