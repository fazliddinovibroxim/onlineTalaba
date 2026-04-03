package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.dto.publicview.PublicCommentCreateRequest;
import com.example.onlinetalaba.dto.publicview.PublicCommentResponse;
import com.example.onlinetalaba.dto.publicview.PublicCommentUpdateRequest;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.PublicCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/comments")
@RequiredArgsConstructor
public class PublicCommentController {

    private final PublicCommentService publicCommentService;

    @GetMapping
    public ResponseEntity<List<PublicCommentResponse>> getAll(@CurrentUser User currentUser) {
        return ResponseEntity.ok(publicCommentService.getThreads(currentUser));
    }

    @PostMapping
    public ResponseEntity<PublicCommentResponse> create(@RequestBody PublicCommentCreateRequest request,
                                                        @CurrentUser User currentUser) {
        return ResponseEntity.ok(publicCommentService.create(request, currentUser));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<PublicCommentResponse> update(@PathVariable Long commentId,
                                                        @RequestBody PublicCommentUpdateRequest request,
                                                        @CurrentUser User currentUser) {
        return ResponseEntity.ok(publicCommentService.update(commentId, request, currentUser));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId,
                                       @CurrentUser User currentUser) {
        publicCommentService.delete(commentId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{commentId}/resolve")
    public ResponseEntity<PublicCommentResponse> resolve(@PathVariable Long commentId,
                                                         @RequestParam(defaultValue = "true") boolean resolved,
                                                         @CurrentUser User currentUser) {
        return ResponseEntity.ok(publicCommentService.markResolved(commentId, resolved, currentUser));
    }
}
