package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.PublicCommentMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublicCommentMessageRepository extends JpaRepository<PublicCommentMessage, Long> {
    List<PublicCommentMessage> findAllByDeletedFalseOrderByIdAsc();
    Optional<PublicCommentMessage> findByIdAndDeletedFalse(Long id);
    long countByParentIdAndDeletedFalse(Long parentId);
}
