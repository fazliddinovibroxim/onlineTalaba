package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.RoomChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomChatMessageRepository extends JpaRepository<RoomChatMessage, Long> {
    List<RoomChatMessage> findAllByRoomIdAndDeletedFalseOrderByIdAsc(Long roomId);
}