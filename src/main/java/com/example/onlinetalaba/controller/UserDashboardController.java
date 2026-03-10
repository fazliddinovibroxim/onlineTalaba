package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.auth.UserDashboardResponse;
import com.example.onlinetalaba.dto.auth.UserDto;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-data")
@RequiredArgsConstructor
public class UserDashboardController {

    private final UserDashboardService dashboardService;

    @GetMapping("/me")
    public ResponseEntity<UserDashboardResponse> getMyDashboard(@CurrentUser User user) {
        return ResponseEntity.ok(dashboardService.getUserDashboard(user));
    }

    @PutMapping("/update")
    public ResponseEntity<User> update(@CurrentUser User user, @RequestBody UserDto dto){
        return ResponseEntity.ok(dashboardService.update(user, dto));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMe(@CurrentUser User user) {
        dashboardService.deleteMyAccount(user);
        return ResponseEntity.ok("Hisobingiz muvaffaqiyatli o'chirildi (Soft delete).");
    }
}
