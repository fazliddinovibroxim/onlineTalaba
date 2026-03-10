package com.example.onlinetalaba.service;

import com.example.onlinetalaba.entity.Attachment;
import com.example.onlinetalaba.entity.LibraryMaterial;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.handler.BadRequestException;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.AttachmentRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
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
    private final RoomMemberRepository roomMemberRepository;

    @Getter
    @Value("${custom_app_config.uploadDirectory}")
    private String uploadDirectory;

    @Value("${custom_app_config.baseUrl}")
    private String baseUrl;

    public Attachment uploadLibraryFile(MultipartFile file, LibraryMaterial libraryMaterial) throws IOException {
        Attachment attachment = storeAttachment(file);
        attachment.setLibraryMaterial(libraryMaterial);
        return attachmentRepository.save(attachment);
    }

    public String uploadChatImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        return attachmentRepository.save(storeAttachment(file)).getFileUrl();
    }

    public Resource loadAsResource(String serverName) throws MalformedURLException {
        Path filePath = resolveStoredFile(serverName);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new NotFoundException("File not found: " + serverName);
        }
        return resource;
    }

    @Transactional(readOnly = true)
    public Attachment getAttachmentByServerName(String serverName) {
        return attachmentRepository.findByServerName(serverName)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
    }

    public void deleteAttachmentByLibraryMaterial(LibraryMaterial libraryMaterial) throws IOException {
        Attachment attachment = attachmentRepository.findByLibraryMaterialId(libraryMaterial.getId())
                .orElseThrow(() -> new NotFoundException("Attachment not found"));

        deleteStoredFile(attachment.getServerName());
        attachmentRepository.delete(attachment);

        log.info("Attachment deleted for library material {}", libraryMaterial.getId());
    }

    public void deleteAttachmentByServerName(String serverName) throws IOException {
        Attachment attachment = attachmentRepository.findByServerName(serverName)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));

        deleteStoredFile(serverName);
        attachmentRepository.delete(attachment);

        log.info("Attachment {} deleted", serverName);
    }

    public void deleteAttachmentByServerName(String serverName, User currentUser) throws IOException {
        Attachment attachment = attachmentRepository.findByServerName(serverName)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));

        validateDeleteAccess(attachment, currentUser);
        deleteStoredFile(serverName);
        attachmentRepository.delete(attachment);

        log.info("Attachment {} deleted by user {}", serverName, currentUser.getId());
    }

    private Attachment storeAttachment(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        createUploadDirIfNeeded();

        String ext = getExtension(file.getOriginalFilename());
        String serverName = UUID.randomUUID() + ext;
        Path targetLocation = resolveStoredFile(serverName);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return Attachment.builder()
                .serverName(serverName)
                .originalName(file.getOriginalFilename() == null ? serverName : file.getOriginalFilename())
                .contentType(resolveContentType(file, targetLocation))
                .size(file.getSize())
                .fileUrl(baseUrl + "/api/v1/library/files/" + serverName)
                .build();
    }

    private void createUploadDirIfNeeded() throws IOException {
        Path uploadPath = baseUploadPath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    private Path baseUploadPath() {
        return Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    private Path resolveStoredFile(String serverName) {
        if (serverName == null || serverName.isBlank()) {
            throw new BadRequestException("Invalid file name");
        }

        Path resolved = baseUploadPath().resolve(serverName).normalize();
        if (!resolved.startsWith(baseUploadPath())) {
            throw new BadRequestException("Invalid file path");
        }
        return resolved;
    }

    private void deleteStoredFile(String serverName) throws IOException {
        Path filePath = resolveStoredFile(serverName);
        Files.deleteIfExists(filePath);
    }

    private String resolveContentType(MultipartFile file, Path targetLocation) throws IOException {
        if (file.getContentType() != null && !file.getContentType().isBlank()) {
            return file.getContentType();
        }

        String probed = Files.probeContentType(targetLocation);
        return probed == null ? "application/octet-stream" : probed;
    }

    private void validateDeleteAccess(Attachment attachment, User currentUser) {
        LibraryMaterial libraryMaterial = attachment.getLibraryMaterial();

        if (libraryMaterial != null) {
            RoomMember member = roomMemberRepository
                    .findByRoomIdAndUserIdAndActiveTrue(libraryMaterial.getRoom().getId(), currentUser.getId())
                    .orElseThrow(() -> new ForbiddenException("Access denied"));

            boolean canDelete = libraryMaterial.getUploadedBy().getId().equals(currentUser.getId())
                    || member.getRole() == RoomMemberRole.OWNER
                    || member.isCanManageRoom()
                    || member.isCanUploadMaterials();

            if (!canDelete) {
                throw new ForbiddenException("You do not have permission to delete this attachment");
            }
            return;
        }

        if (attachment.getCreatedBy() == null || !attachment.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not have permission to delete this attachment");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
