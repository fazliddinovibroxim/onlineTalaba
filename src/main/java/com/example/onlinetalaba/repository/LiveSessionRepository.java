package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.LiveSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {
    Optional<LiveSession> findByLessonScheduleId(Long lessonScheduleId);
}