package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.LiveSessionComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiveSessionCommentRepository extends JpaRepository<LiveSessionComment, Long> {

    List<LiveSessionComment> findAllByLiveSessionIdAndDeletedFalseOrderByIdAsc(Long liveSessionId);
}
