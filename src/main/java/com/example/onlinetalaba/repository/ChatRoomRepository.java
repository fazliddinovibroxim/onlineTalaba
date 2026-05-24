package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    @Query(value = """
            SELECT *
            FROM chat_rooms
            WHERE user_one_id = :userId OR user_two_id = :userId
            ORDER BY last_message_at DESC NULLS LAST, id DESC
            """, nativeQuery = true)
    List<ChatRoom> findAllForUser(@Param("userId") Long userId);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM ChatRoom c
            WHERE c.id = :chatRoomId
              AND (c.userOne.id = :userId OR c.userTwo.id = :userId)
            """)
    boolean existsForUser(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}
