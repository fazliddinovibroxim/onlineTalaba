package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.enums.RoomVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findAllByActiveTrue();
    List<Room> findAllByVisibilityAndActiveTrue(RoomVisibility visibility);
    List<Room> findByOwnerId(Long userId);
    long countByActiveTrue();
    long countByVisibilityAndActiveTrue(RoomVisibility visibility);
}
