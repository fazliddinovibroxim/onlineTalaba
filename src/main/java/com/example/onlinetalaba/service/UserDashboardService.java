package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.auth.RoomUserResponse;
import com.example.onlinetalaba.dto.auth.UserDashboardResponse;
import com.example.onlinetalaba.dto.auth.UserDto;
import com.example.onlinetalaba.entity.Role;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppPermissions;
import com.example.onlinetalaba.repository.RoleRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import com.example.onlinetalaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDashboardService {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserDashboardResponse getUserDashboard(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("User not found with id={}", userId);
                        return new RuntimeException("User not found");
                    });

            Role role = roleRepository.findByAppRoleName(user.getRoles().getAppRoleName())
                    .orElseThrow(() -> {
                        log.error("Role not found for userId={} with roleName={}", userId, user.getRoles().getAppRoleName());
                        return new RuntimeException("Role not found for user");
                    });

            Set<AppPermissions> appPermissions = role.getAppPermissions();

            // userga tegishli buyurtmalar
            List<RoomUserResponse> rooms = roomRepository.findByOwnerId(userId)
                    .stream()
                    .map(o -> new RoomUserResponse(
                            o.getId(),
                            o.getTitle(),
                            o.getSubject(),
                            o.isActive()
                    ))
                    .toList();

            return new UserDashboardResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    role.getAppRoleName(),// role
                    user.getGender(),
                    appPermissions,           // permissions
                    rooms                    // orders
            );

        } catch (Exception e) {
            log.error("Failed to get dashboard for userId={}", userId, e);
            throw e;
        }
    }

    public User update(User userY, UserDto dto) {
        try {
            User user = userRepository.findByEmail(userY.getEmail());
            if (user == null) {
                log.error("User not found for update with email={}", userY.getEmail());
                throw new RuntimeException("User not found for update");
            }

            user.setFullName(dto.getFullName());
            user.setEmail(dto.getEmail());
            user.setUsername(dto.getUsername());
            user.setAddress(dto.getAddress());
            user.setPhoneNumber(dto.getPhoneNumber());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));

            User updated = userRepository.save(user);
            log.info("Updated user successfully: userId={}, email={}", updated.getId(), updated.getEmail());
            return updated;
        } catch (Exception e) {
            log.error("Failed to update user with email={}", userY.getEmail(), e);
            throw e;
        }
    }

    public void deleteMyAccount(User currentUser) {
        try {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> {
                        log.error("User not found for deletion: id={}", currentUser.getId());
                        return new RuntimeException("User not found");
                    });

            // Soft delete amali
            user.setIsDeleted(true);
            user.setEnabled(false); // O'chirilgan foydalanuvchi tizimga kira olmasligi uchun

            userRepository.save(user);
            log.info("User account soft-deleted successfully: userId={}", user.getId());
        } catch (Exception e) {
            log.error("Failed to delete user account for userId={}", currentUser.getId(), e);
            throw e;
        }
    }
}