package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.log.LogProgressDto;
import com.example.onlinetalaba.service.LogProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@PreAuthorize("hasAuthority('DELETE')")
@RequestMapping("/api/v1/admin/history-log-progress")
@RequiredArgsConstructor
public class LogProgressController {

    private final LogProgressService logProgressService;

    public ResponseEntity<Page<LogProgressDto>> getAllLogProgress(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
         return ResponseEntity.ok(logProgressService.getAll(fromDate, toDate, PageRequest.of(page, size)));
    }

    public ResponseEntity<List<LogProgressDto>> getAllByActionType(
            @RequestParam String actionType
    ) {
        return ResponseEntity.ok(logProgressService.getByActionType(actionType));
    }

}
