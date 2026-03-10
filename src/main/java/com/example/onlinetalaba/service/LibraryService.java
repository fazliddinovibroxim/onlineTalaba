package com.example.onlinetalaba.service;

import com.example.onlinetalaba.dto.library.AttachmentResponse;
import com.example.onlinetalaba.dto.library.LibraryMaterialRequest;
import com.example.onlinetalaba.dto.library.LibraryMaterialResponse;
import com.example.onlinetalaba.entity.Attachment;
import com.example.onlinetalaba.entity.LibraryMaterial;
import com.example.onlinetalaba.entity.Room;
import com.example.onlinetalaba.entity.RoomMember;
import com.example.onlinetalaba.entity.User;
import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.handler.ForbiddenException;
import com.example.onlinetalaba.handler.NotFoundException;
import com.example.onlinetalaba.repository.LibraryMaterialRepository;
import com.example.onlinetalaba.repository.RoomMemberRepository;
import com.example.onlinetalaba.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LibraryService {

    private final LibraryMaterialRepository libraryMaterialRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final AttachmentService attachmentService;

    public LibraryMaterialResponse uploadToRoom(Long roomId,
                                                LibraryMaterialRequest request,
                                                MultipartFile file,
                                                User currentUser) throws IOException {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        if (!(member.getRole() == RoomMemberRole.OWNER
                || member.getRole() == RoomMemberRole.TEACHER
                || member.isCanUploadMaterials())) {
            throw new ForbiddenException("You do not have permission to upload materials");
        }

        LibraryMaterial material = LibraryMaterial.builder()
                .room(room)
                .uploadedBy(currentUser)
                .title(request.getTitle())
                .description(request.getDescription())
                .materialType(request.getMaterialType())
                .active(true)
                .build();

        material = libraryMaterialRepository.save(material);

        Attachment attachment = attachmentService.uploadLibraryFile(file, material);
        material.setAttachment(attachment);

        material = libraryMaterialRepository.save(material);

        return toResponse(material);
    }

    public List<LibraryMaterialResponse> getAllByRoom(Long roomId, User currentUser) {
        roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        return libraryMaterialRepository.findAllByRoomIdAndActiveTrue(roomId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public LibraryMaterialResponse getById(Long roomId, Long materialId, User currentUser) {
        roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        LibraryMaterial material = libraryMaterialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Library material not found"));

        if (!material.getRoom().getId().equals(roomId) || !material.isActive()) {
            throw new NotFoundException("Library material not found in this room");
        }

        return toResponse(material);
    }

    public void deleteMaterial(Long roomId, Long materialId, User currentUser) throws IOException {
        LibraryMaterial material = libraryMaterialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Library material not found"));

        if (!material.getRoom().getId().equals(roomId)) {
            throw new NotFoundException("Library material not found in this room");
        }

        RoomMember member = roomMemberRepository.findByRoomIdAndUserIdAndActiveTrue(roomId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("Access denied"));

        boolean canDelete = material.getUploadedBy().getId().equals(currentUser.getId())
                || member.getRole() == RoomMemberRole.OWNER
                || member.isCanManageRoom()
                || member.isCanUploadMaterials();

        if (!canDelete) {
            throw new ForbiddenException("You do not have permission to delete this material");
        }

        attachmentService.deleteAttachmentByLibraryMaterial(material);

        material.setActive(false);
        material.setAttachment(null);
        libraryMaterialRepository.save(material);
    }

    private LibraryMaterialResponse toResponse(LibraryMaterial material) {
        return LibraryMaterialResponse.builder()
                .id(material.getId())
                .roomId(material.getRoom().getId())
                .uploadedById(material.getUploadedBy().getId())
                .uploadedByName(material.getUploadedBy().getFullName())
                .title(material.getTitle())
                .description(material.getDescription())
                .materialType(material.getMaterialType())
                .active(material.isActive())
                .attachment(material.getAttachment() == null ? null :
                        new AttachmentResponse(
                                material.getAttachment().getServerName(),
                                material.getAttachment().getOriginalName(),
                                material.getAttachment().getContentType(),
                                material.getAttachment().getSize(),
                                material.getAttachment().getFileUrl()
                        ))
                .build();
    }
}
