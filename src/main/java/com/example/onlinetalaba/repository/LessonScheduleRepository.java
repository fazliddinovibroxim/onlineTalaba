package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.LessonSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonScheduleRepository extends JpaRepository<LessonSchedule, Long> {
    List<LessonSchedule> findAllByRoomIdOrderByStartTimeAsc(Long roomId);
}