package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.Role;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByFullName(String username);

    Optional<User> findByUsername(String username);

    boolean existsByFullName(String username);

    User findByRoles(Role roles);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndEmailCode(String email, String code);

    User findByEmail(String email);

    List<User> findAllByRoles_AppRoleName(AppRoleName appRoleName);

    Optional<User> findByEmailOrUsername(String email, String username);

    boolean existsByUsername(String username);

    Optional<User> findByEmailAndIsDeletedFalse(String email);
    long countByIsDeletedFalse();
}
