package com.example.onlinetalaba.entity;

import com.example.onlinetalaba.enums.LessonCommentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lesson_comment_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonCommentMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_schedule_id")
    private LessonSchedule lessonSchedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonCommentType commentType;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = false)
    private boolean edited = false;

    @Column(nullable = false)
    private boolean deleted = false;
}