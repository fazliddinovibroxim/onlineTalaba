package com.example.onlinetalaba.service;

import com.example.onlinetalaba.entity.Attachment;
import com.example.onlinetalaba.entity.LibraryMaterial;
import com.example.onlinetalaba.repository.AttachmentRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    @Getter
    @Value("${custom_app_config.uploadDirectory}")
    private String uploadDirectory;

    @Value("${custom_app_config.baseUrl}")
    private String baseUrl;

    public Attachment uploadLibraryFile(MultipartFile file, LibraryMaterial libraryMaterial) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        createUploadDirIfNeeded();

        String ext = getExtension(file.getOriginalFilename());
        String serverName = UUID.randomUUID() + ext;

        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        Path targetLocation = uploadPath.resolve(serverName);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        Attachment attachment = Attachment.builder()
                .serverName(serverName)
                .originalName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .fileUrl(baseUrl + "/api/v1/library/files/" + serverName)
                .libraryMaterial(libraryMaterial)
                .build();

        return attachmentRepository.save(attachment);
    }

    public Resource loadAsResource(String serverName) throws MalformedURLException {
        Path filePath = Paths.get(uploadDirectory).resolve(serverName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("File not found: " + serverName);
        }
        return resource;
    }

    public void deleteAttachmentByLibraryMaterial(LibraryMaterial libraryMaterial) throws IOException {
        Attachment attachment = attachmentRepository.findByLibraryMaterialId(libraryMaterial.getId())
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        Path filePath = Paths.get(uploadDirectory).resolve(attachment.getServerName()).normalize();
        Files.deleteIfExists(filePath); // serverdan o'chadi
        attachmentRepository.delete(attachment); // db dan o'chadi

        log.info("Attachment deleted for library material {}", libraryMaterial.getId());
    }

    public void deleteAttachmentByServerName(String serverName) throws IOException {
        Attachment attachment = attachmentRepository.findByServerName(serverName)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        Path filePath = Paths.get(uploadDirectory).resolve(serverName).normalize();
        Files.deleteIfExists(filePath);
        attachmentRepository.delete(attachment);

        log.info("Attachment {} deleted", serverName);
    }

    private void createUploadDirIfNeeded() throws IOException {
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}