package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.LessonSchedule;
import com.example.onlinetalaba.enums.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface LessonScheduleRepository extends JpaRepository<LessonSchedule, Long> {
    List<LessonSchedule> findAllByRoomIdOrderByStartTimeAsc(Long roomId);
    List<LessonSchedule> findTop3ByRoomIdAndStartTimeAfterOrderByStartTimeAsc(Long roomId, LocalDateTime after);
    List<LessonSchedule> findTop1ByRoomIdOrderByStartTimeDesc(Long roomId);
    List<LessonSchedule> findAllByRoomIdInAndStartTimeAfterOrderByStartTimeAsc(Collection<Long> roomIds, LocalDateTime after);
    List<LessonSchedule> findTop10ByStartTimeAfterOrderByStartTimeAsc(LocalDateTime after);
    List<LessonSchedule> findAllByStatusAndStartTimeBetweenAndReminderSentAtIsNull(
            LessonStatus status,
            LocalDateTime from,
            LocalDateTime to
    );
    long countByRoomIdAndStatus(Long roomId, LessonStatus status);
    long countByRoomIdAndStatusAndStartTimeBetween(Long roomId, LessonStatus status, LocalDateTime start, LocalDateTime end);
    long countByStatus(LessonStatus status);
}
