package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.auth.UserDashboardResponse;
import com.example.onlinetalaba.dto.auth.UserDto;
import com.example.onlinetalaba.dto.dashboard.UserSearchItemResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserDashboardController {

    private final UserDashboardService dashboardService;

    @GetMapping("/api/v1/user-data/me")
    public ResponseEntity<UserDashboardResponse> getMyDashboard(@CurrentUser User user) {
        return ResponseEntity.ok(dashboardService.getUserDashboard(user));
    }

    @GetMapping("/api/v1/users/getAll")
    public ResponseEntity<Page<UserSearchItemResponse>> searchUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Long excludeUserId,
            @RequestParam(defaultValue = "false") boolean excludeSelf,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser User currentUser
    ) {
        Long resolvedExcludeUserId = excludeUserId != null
                ? excludeUserId
                : (excludeSelf ? currentUser.getId() : null);

        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.ASC, "id")
        );

        return ResponseEntity.ok(dashboardService.searchUsers(
                q,
                fullName,
                username,
                email,
                phoneNumber,
                address,
                resolvedExcludeUserId,
                pageable
        ));
    }

    @PutMapping("/api/v1/user-data/update")
    public ResponseEntity<User> update(@CurrentUser User user, @RequestBody UserDto dto) {
        return ResponseEntity.ok(dashboardService.update(user, dto));
    }

    @DeleteMapping("/api/v1/user-data/delete")
    public ResponseEntity<String> deleteMe(@CurrentUser User user) {
        dashboardService.deleteMyAccount(user);
        return ResponseEntity.ok("Hisobingiz muvaffaqiyatli o'chirildi (Soft delete).");
    }
}
