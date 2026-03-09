package com.example.onlinetalaba.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FCMRepository extends JpaRepository<FCM, Long> {
    Optional<FCM> findByUserId(Long userId);
    Optional<FCM> findByFcmToken(String fcmToken);
}
