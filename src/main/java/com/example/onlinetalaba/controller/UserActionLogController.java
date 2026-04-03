package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.log.UserActionLogResponse;
import com.example.onlinetalaba.service.UserActionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequestMapping("/api/v1/super_admin/user-actions")
@RequiredArgsConstructor
public class UserActionLogController {

    private final UserActionLogService service;

    @GetMapping("/getAll")
    public ResponseEntity<List<UserActionLogResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserActionLogResponse>> getByUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }
}
