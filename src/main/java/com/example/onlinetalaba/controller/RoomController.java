package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.room.RoomInviteRequest;
import com.example.onlinetalaba.dto.room.RoomRequest;
import com.example.onlinetalaba.dto.room.RoomResponse;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.RoomVisibility;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<Page<RoomResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) RoomVisibility visibility,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(defaultValue = "false") boolean includeMembers,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser User currentUser
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Direction.DESC, "id")
        );
        return ResponseEntity.ok(roomService.search(q, visibility, ownerId, includeMembers, pageable, currentUser));
    }

    @PostMapping
    public ResponseEntity<RoomResponse> create(@RequestBody RoomRequest request,
                                               @CurrentUser User currentUser) {
        return ResponseEntity.ok(roomService.create(request, currentUser));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getById(@PathVariable Long roomId,
                                                @CurrentUser User currentUser) {
        return ResponseEntity.ok(roomService.getById(roomId, currentUser));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomResponse> update(@PathVariable Long roomId,
                                               @RequestBody RoomRequest request,
                                               @CurrentUser User currentUser) {
        return ResponseEntity.ok(roomService.update(roomId, request, currentUser));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> delete(@PathVariable Long roomId,
                                       @CurrentUser User currentUser) {
        roomService.delete(roomId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/invite")
    public ResponseEntity<Void> invite(@PathVariable Long roomId,
                                       @RequestBody RoomInviteRequest request,
                                       @CurrentUser User currentUser) {
        roomService.inviteMember(roomId, request, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<RoomResponse> join(@PathVariable Long roomId,
                                             @CurrentUser User currentUser) {
        return ResponseEntity.ok(roomService.join(roomId, currentUser));
    }
}
