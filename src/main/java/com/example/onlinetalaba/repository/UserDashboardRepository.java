package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDashboardRepository extends JpaRepository<User, Long> {

    String USER_SEARCH_WHERE = """
            FROM users u
            WHERE COALESCE(u.is_deleted, false) = false
              AND COALESCE(u.is_enabled, false) = true
              AND (:excludeUserId IS NULL OR u.id <> :excludeUserId)
              AND (
                    :q IS NULL OR :q = ''
                    OR LOWER(COALESCE(u.full_name, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(COALESCE(u.phone_number, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(COALESCE(u.address, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                  )
              AND (
                    :fullName IS NULL OR :fullName = ''
                    OR LOWER(COALESCE(u.full_name, '')) LIKE LOWER(CONCAT('%', :fullName, '%'))
                  )
              AND (
                    :username IS NULL OR :username = ''
                    OR LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :username, '%'))
                  )
              AND (
                    :email IS NULL OR :email = ''
                    OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :email, '%'))
                  )
              AND (
                    :phoneNumber IS NULL OR :phoneNumber = ''
                    OR LOWER(COALESCE(u.phone_number, '')) LIKE LOWER(CONCAT('%', :phoneNumber, '%'))
                  )
              AND (
                    :address IS NULL OR :address = ''
                    OR LOWER(COALESCE(u.address, '')) LIKE LOWER(CONCAT('%', :address, '%'))
                  )
            """;

    @Query(value = """
            SELECT u.id,
                   u.full_name,
                   u.username,
                   u.email,
                   u.phone_number,
                   u.address
            """ + USER_SEARCH_WHERE + """
            ORDER BY u.id ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> findAllByNativeQuery(
            @Param("q") String q,
            @Param("fullName") String fullName,
            @Param("username") String username,
            @Param("email") String email,
            @Param("phoneNumber") String phoneNumber,
            @Param("address") String address,
            @Param("excludeUserId") Long excludeUserId,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    @Query(value = "SELECT COUNT(*) " + USER_SEARCH_WHERE, nativeQuery = true)
    long countByNativeQuery(
            @Param("q") String q,
            @Param("fullName") String fullName,
            @Param("username") String username,
            @Param("email") String email,
            @Param("phoneNumber") String phoneNumber,
            @Param("address") String address,
            @Param("excludeUserId") Long excludeUserId
    );
}
