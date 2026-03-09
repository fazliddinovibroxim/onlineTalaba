package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.Role;
import com.example.onlinetalaba.enums.AppRoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByAppRoleName(AppRoleName name);

    boolean existsByAppRoleName(AppRoleName roleName);
}