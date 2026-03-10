package com.example.onlinetalaba.controller;

import com.example.onlinetalaba.entity.Attachment;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.security.CurrentUser;
import com.example.onlinetalaba.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
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
        Attachment attachment = attachmentService.getAttachmentByServerName(serverName);
        Resource resource = attachmentService.loadAsResource(serverName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getOriginalName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(attachment.getSize()))
                .body(resource);
    }

    @DeleteMapping("/{serverName}")
    public ResponseEntity<Void> deleteFile(@PathVariable String serverName,
                                           @CurrentUser User currentUser) throws Exception {
        attachmentService.deleteAttachmentByServerName(serverName, currentUser);
        return ResponseEntity.noContent().build();
    }
}
