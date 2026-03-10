package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.HandRaise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HandRaiseRepository extends JpaRepository<HandRaise, Long> {
    List<HandRaise> findAllByLiveSessionIdAndActiveTrueOrderByRequestedAtAsc(Long liveSessionId);
    Optional<HandRaise> findByLiveSessionIdAndUserIdAndActiveTrue(Long liveSessionId, Long userId);
    Optional<HandRaise> findTopByLiveSessionIdAndUserIdOrderByRequestedAtDesc(Long liveSessionId, Long userId);
}
