package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.auth.UserResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAll() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .filter(user -> !user.getIsDeleted())
                .map(this::toResponse)
                .toList();

        log.info("Total users found: {}", users.size());
        return users;
    }

    public UserResponse getById(Long id) {
        log.info("Fetching user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new RuntimeException("User not found");
                });

        return toResponse(user);
    }

    public void delete(Long id, Boolean isDelete) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found for delete, id: {}", id);
                    return new RuntimeException("User not found");
                });

        if (isDelete) {
            userRepository.delete(user);
            return;
        }
        user.setIsDeleted(true);
        userRepository.save(user);

        log.info("User successfully deleted (soft delete), id: {}", id);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .gender(user.getGender())
                .role(
                        user.getRoles() != null
                                ? user.getRoles().getAppRoleName().name()
                                : null
                )
                .enabled(user.isEnabled())
                .build();
    }
}
