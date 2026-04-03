package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.RoomJoinRequest;
import com.example.onlinetalaba.enums.RoomJoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface RoomJoinRequestRepository extends JpaRepository<RoomJoinRequest, Long> {
    boolean existsByRoomIdAndRequesterIdAndStatus(Long roomId, Long requesterId, RoomJoinRequestStatus status);
    List<RoomJoinRequest> findAllByRoomIdAndStatusOrderByDatetimeCreatedAsc(Long roomId, RoomJoinRequestStatus status);
    List<RoomJoinRequest> findAllByRequesterIdOrderByDatetimeCreatedDesc(Long requesterId);
    List<RoomJoinRequest> findAllByStatusOrderByDatetimeCreatedAsc(RoomJoinRequestStatus status);
    long countByRoomIdAndStatus(Long roomId, RoomJoinRequestStatus status);
    long countByRoomIdAndStatusAndDatetimeCreatedAfter(Long roomId, RoomJoinRequestStatus status, LocalDateTime after);
    long countByStatus(RoomJoinRequestStatus status);
    Optional<RoomJoinRequest> findByIdAndRoomId(Long id, Long roomId);
}
