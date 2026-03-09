package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    Optional<RoomMember> findByRoomIdAndUserIdAndActiveTrue(Long roomId, Long userId);
    List<RoomMember> findAllByRoomIdAndActiveTrue(Long roomId);
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
}