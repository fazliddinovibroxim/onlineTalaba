package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.LogProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LogProgressRepository extends JpaRepository<LogProgress, Long> {

    @Query(value = """
        SELECT *
        FROM log_progress l
        WHERE l.created_at BETWEEN :fromDate AND :toDate
        ORDER BY l.created_at DESC
        """, nativeQuery = true)
    Page<LogProgress> findAllByDateRangeNative(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query(value = """
        SELECT *
        FROM log_progress l
        WHERE l.action_type = :actionType
        ORDER BY l.created_at DESC
        """, nativeQuery = true)
    List<LogProgress> findAllByActionTypeNative(@Param("actionType") String actionType);
}
