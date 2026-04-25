package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.LiveSessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LiveSessionParticipantRepository extends JpaRepository<LiveSessionParticipant, Long> {

    Optional<LiveSessionParticipant> findByLiveSessionIdAndUserId(Long liveSessionId, Long userId);

    List<LiveSessionParticipant> findAllByLiveSessionIdOrderByJoinedAtAsc(Long liveSessionId);

    long countByLiveSessionIdAndLastSeenAtAfter(Long liveSessionId, LocalDateTime lastSeenAfter);
}

