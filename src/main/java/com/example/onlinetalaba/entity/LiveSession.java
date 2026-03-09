package com.example.onlinetalaba.entity;

import com.example.onlinetalaba.enums.LiveSessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "live_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveSession extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_schedule_id", unique = true)
    private LessonSchedule lessonSchedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id")
    private User host;

    @Column(nullable = false, unique = true)
    private String livekitRoomName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiveSessionStatus status;

    @Column(nullable = false)
    private boolean active;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}