package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.LessonCommentMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonCommentMessageRepository extends JpaRepository<LessonCommentMessage, Long> {
    List<LessonCommentMessage> findAllByLessonScheduleIdAndDeletedFalseOrderByIdAsc(Long lessonScheduleId);
}