package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.RoomJoinRequest;
import com.example.onlinetalaba.enums.RoomJoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomJoinRequestRepository extends JpaRepository<RoomJoinRequest, Long> {
    boolean existsByRoomIdAndRequesterIdAndStatus(Long roomId, Long requesterId, RoomJoinRequestStatus status);
    List<RoomJoinRequest> findAllByRoomIdAndStatusOrderByDatetimeCreatedAsc(Long roomId, RoomJoinRequestStatus status);
    List<RoomJoinRequest> findAllByRequesterIdOrderByDatetimeCreatedDesc(Long requesterId);
    List<RoomJoinRequest> findAllByStatusOrderByDatetimeCreatedAsc(RoomJoinRequestStatus status);
    long countByStatus(RoomJoinRequestStatus status);
    Optional<RoomJoinRequest> findByIdAndRoomId(Long id, Long roomId);
}
