package com.example.onlinetalaba.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {

    @Query(value = "select * from notification as n where n.user_id=?1",nativeQuery = true)
    Page<Notification> findAllByUserId(Long userId,Pageable pageable);
    List<Notification> findTop10ByUserIdOrderByIdDesc(Long userId);
    List<Notification> findTop10ByOrderByIdDesc();
    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("update notification n set n.isRead = true where n.id = :id")
    void markAsRead(@Param("id") Long id);

    @Modifying
    @Query("update notification n set n.isRead = true where n.user.id = :userId")
    void markAllAsRead(@Param("userId") Long userId);
}
