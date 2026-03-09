package com.example.onlinetalaba.notification;

import com.example.onlinetalaba.entity.BaseEntity;
import com.example.onlinetalaba.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
@Entity(name = "notification")
public class Notification extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    private String body;

    private String status;

    private LocalDate createdAt;

    @Builder.Default
    private Boolean isRead = false;
}
