package com.example.onlinetalaba.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "user_action_log")
public class UserActionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String requestedUrl;

    @Column(columnDefinition = "TEXT")
    private String actionInfo;

    private String  actionType;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime time;
}