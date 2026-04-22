package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.user.UserDirectoryItemResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDirectoryService {

    private final UserRepository userRepository;

    public Page<UserDirectoryItemResponse> search(String q,
                                                 String fullName,
                                                 String username,
                                                 String email,
                                                 String address,
                                                 Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> cb.isFalse(root.get("isDeleted"));

        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(cb.coalesce(root.get("fullName"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("username"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("email"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("address"), "")), like)
            ));
        }

        if (fullName != null && !fullName.isBlank()) {
            String like = "%" + fullName.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(cb.coalesce(root.get("fullName"), "")), like));
        }
        if (username != null && !username.isBlank()) {
            String like = "%" + username.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(cb.coalesce(root.get("username"), "")), like));
        }
        if (email != null && !email.isBlank()) {
            String like = "%" + email.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(cb.coalesce(root.get("email"), "")), like));
        }
        if (address != null && !address.isBlank()) {
            String like = "%" + address.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(cb.coalesce(root.get("address"), "")), like));
        }

        return userRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    private UserDirectoryItemResponse toResponse(User user) {
        return UserDirectoryItemResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .address(user.getAddress())
                .build();
    }
}

