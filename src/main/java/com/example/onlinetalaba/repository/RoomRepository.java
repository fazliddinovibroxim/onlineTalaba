package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findAllByActiveTrue();

    List<Room> findByOwnerId(Long userId);
}