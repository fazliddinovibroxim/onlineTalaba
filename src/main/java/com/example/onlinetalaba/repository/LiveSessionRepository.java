package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.LiveSession;
import com.example.onlinetalaba.enums.RoomVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {
    Optional<LiveSession> findByLessonScheduleId(Long lessonScheduleId);
    List<LiveSession> findAllByLessonScheduleRoomIdInAndActiveTrue(Collection<Long> roomIds);
    List<LiveSession> findAllByActiveTrue();
    List<LiveSession> findAllByActiveTrueAndLessonScheduleRoomVisibility(RoomVisibility visibility);
    long countByActiveTrue();
}
