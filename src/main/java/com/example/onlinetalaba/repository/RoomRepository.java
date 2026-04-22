package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.enums.RoomVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    List<Room> findAllByActiveTrue();
    List<Room> findAllByVisibilityAndActiveTrue(RoomVisibility visibility);
    List<Room> findByOwnerId(Long userId);
    long countByActiveTrue();
    long countByVisibilityAndActiveTrue(RoomVisibility visibility);
}
