package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;

@RestController
@RequestMapping("/api/v1/library/files")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping("/{serverName}")
    public ResponseEntity<Resource> viewFile(@PathVariable String serverName) throws MalformedURLException {
        Resource resource = attachmentService.loadAsResource(serverName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + serverName + "\"")
                .body(resource);
    }

    @DeleteMapping("/{serverName}")
    public ResponseEntity<Void> deleteFile(@PathVariable String serverName) throws Exception {
        attachmentService.deleteAttachmentByServerName(serverName);
        return ResponseEntity.noContent().build();
    }
}