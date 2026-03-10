package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    Optional<Attachment> findByServerName(String serverName);
    Optional<Attachment> findByLibraryMaterialId(Long libraryMaterialId);
}
