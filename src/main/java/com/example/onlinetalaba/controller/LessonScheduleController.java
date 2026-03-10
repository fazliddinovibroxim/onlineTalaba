package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.schedule.LessonScheduleRequest;
import com.example.onlinetalaba.dto.schedule.LessonScheduleResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.LessonScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/schedules")
@RequiredArgsConstructor
public class LessonScheduleController {

    private final LessonScheduleService lessonScheduleService;

    @PostMapping
    public ResponseEntity<Void> create(@PathVariable Long roomId,
                                       @RequestBody LessonScheduleRequest request,
                                       @CurrentUser User currentUser) {
        lessonScheduleService.create(roomId, request, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<LessonScheduleResponse>> getAll(@PathVariable Long roomId,
                                                               @CurrentUser User currentUser) {
        return ResponseEntity.ok(lessonScheduleService.getAllByRoom(roomId, currentUser));
    }
}
