package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.publicview.PublicLibraryMaterialResponse;
import com.example.onlinetalaba.dto.publicview.PublicLibraryUploadRequest;
import com.example.onlinetalaba.dto.publicview.PublicLibraryUploaderResponse;
import com.example.onlinetalaba.dto.publicview.PublicLibraryUserRoomResponse;
import com.example.onlinetalaba.entity.PublicLibraryMaterial;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.LibraryMaterialType;
import com.example.onlinetalaba.handler.BadRequestException;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.PublicLibraryMaterialRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublicLibraryService {

    private final PublicLibraryMaterialRepository publicLibraryMaterialRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final AttachmentService attachmentService;

    @Transactional
    public PublicLibraryMaterialResponse upload(PublicLibraryUploadRequest request,
                                                MultipartFile file,
                                                User currentUser) throws IOException {
        if (request == null) {
            throw new BadRequestException("Upload data is required");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BadRequestException("Title is required");
        }

        if (request.getMaterialType() == null) {
            throw new BadRequestException("Material type is required");
        }

        String fileUrl = attachmentService.uploadChatFile(file);
        String serverName = extractServerName(fileUrl);

        PublicLibraryMaterial material = PublicLibraryMaterial.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .materialType(request.getMaterialType())
                .uploadedBy(currentUser)
                .fileUrl(fileUrl)
                .attachmentServerName(serverName)
                .originalName(file.getOriginalFilename() == null ? serverName : file.getOriginalFilename())
                .contentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType())
                .size(file.getSize())
                .active(true)
                .build();

        publicLibraryMaterialRepository.save(material);
        return toResponse(material);
    }

    @Transactional(readOnly = true)
    public List<PublicLibraryMaterialResponse> getAll(LibraryMaterialType materialType) {
        List<PublicLibraryMaterial> materials = materialType == null
                ? publicLibraryMaterialRepository.findAllByActiveTrueOrderByDatetimeCreatedDesc()
                : publicLibraryMaterialRepository.findAllByMaterialTypeAndActiveTrueOrderByDatetimeCreatedDesc(materialType);

        return materials.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PublicLibraryMaterialResponse> getMyMaterials(User currentUser) {
        return publicLibraryMaterialRepository.findAllByUploadedByIdAndActiveTrueOrderByDatetimeCreatedDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicLibraryMaterialResponse getById(Long materialId) {
        PublicLibraryMaterial material = publicLibraryMaterialRepository.findByIdAndActiveTrue(materialId)
                .orElseThrow(() -> new NotFoundException("Public library material not found"));
        return toResponse(material);
    }

    @Transactional
    public void delete(Long materialId, User currentUser) throws IOException {
        PublicLibraryMaterial material = publicLibraryMaterialRepository.findByIdAndActiveTrue(materialId)
                .orElseThrow(() -> new NotFoundException("Public library material not found"));

        boolean isOwner = material.getUploadedBy().getId().equals(currentUser.getId());
        boolean isAdmin = isSuperScope(currentUser);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to delete this material");
        }

        if (isAdmin && !isOwner) {
            attachmentService.deleteAttachmentByServerName(material.getAttachmentServerName());
        } else {
            attachmentService.deleteAttachmentByServerName(material.getAttachmentServerName(), currentUser);
        }
        material.setActive(false);
        publicLibraryMaterialRepository.save(material);
    }

    private PublicLibraryMaterialResponse toResponse(PublicLibraryMaterial material) {
        User uploader = material.getUploadedBy();
        List<RoomMember> roomMembers = roomMemberRepository.findAllByUserIdAndActiveTrue(uploader.getId());

        Map<Long, PublicLibraryUserRoomResponse> uniqueRooms = new LinkedHashMap<>();
        for (RoomMember roomMember : roomMembers) {
            if (!roomMember.getRoom().isActive()) {
                continue;
            }

            uniqueRooms.putIfAbsent(roomMember.getRoom().getId(), PublicLibraryUserRoomResponse.builder()
                    .roomId(roomMember.getRoom().getId())
                    .roomTitle(roomMember.getRoom().getTitle())
                    .roomSubject(roomMember.getRoom().getSubject())
                    .roomVisibility(roomMember.getRoom().getVisibility())
                    .myRole(roomMember.getRole())
                    .build());
        }

        return PublicLibraryMaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .materialType(material.getMaterialType())
                .fileUrl(material.getFileUrl())
                .originalName(material.getOriginalName())
                .contentType(material.getContentType())
                .size(material.getSize())
                .uploadedAt(material.getDatetimeCreated())
                .updatedAt(material.getDatetimeUpdated())
                .uploader(PublicLibraryUploaderResponse.builder()
                        .userId(uploader.getId())
                        .fullName(uploader.getFullName())
                        .username(uploader.getUsername())
                        .email(uploader.getEmail())
                        .phoneNumber(uploader.getPhoneNumber())
                        .role(uploader.getRoles().getAppRoleName())
                        .roomCount(uniqueRooms.size())
                        .rooms(uniqueRooms.values().stream().toList())
                        .build())
                .build();
    }

    private boolean isSuperScope(User currentUser) {
        AppRoleName role = currentUser.getRoles().getAppRoleName();
        return role == AppRoleName.SUPER_ADMIN || role == AppRoleName.ADMIN;
    }

    private String extractServerName(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new BadRequestException("Invalid file url");
        }

        int idx = fileUrl.lastIndexOf('/');
        if (idx < 0 || idx == fileUrl.length() - 1) {
            throw new BadRequestException("Invalid file url");
        }

        return fileUrl.substring(idx + 1);
    }
}
