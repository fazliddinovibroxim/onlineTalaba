package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.user.UserDirectoryItemResponse;
import com.example.onlinetalaba.service.UserDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserDirectoryController {

    private final UserDirectoryService userDirectoryService;

    @GetMapping("/getAll")
    public ResponseEntity<Page<UserDirectoryItemResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.ASC, "id")
        );
        return ResponseEntity.ok(userDirectoryService.search(q, fullName, username, email, address, pageable));
    }
}

