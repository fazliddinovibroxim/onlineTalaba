package com.example.onlinetalaba.notification;

import com.example.onlinetalaba.entity.BaseEntity;
import com.example.onlinetalaba.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fcm")
public class FCM extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String fcmToken;
}
