package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    @Query("""
            SELECT c FROM ChatRoom c
            WHERE c.userOne.id = :userId OR c.userTwo.id = :userId
            ORDER BY COALESCE(c.lastMessageAt, c.id) DESC, c.id DESC
            """)
    List<ChatRoom> findAllForUser(@Param("userId") Long userId);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM ChatRoom c
            WHERE c.id = :chatRoomId
              AND (c.userOne.id = :userId OR c.userTwo.id = :userId)
            """)
    boolean existsForUser(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}
