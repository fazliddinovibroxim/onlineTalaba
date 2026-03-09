package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.WhiteboardEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WhiteboardEventRepository extends JpaRepository<WhiteboardEvent, Long> {
    List<WhiteboardEvent> findAllByLiveSessionIdAndDeletedFalseOrderByIdAsc(Long liveSessionId);
}