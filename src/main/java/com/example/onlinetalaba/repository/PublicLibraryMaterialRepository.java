package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.PublicLibraryMaterial;
import com.example.onlinetalaba.enums.LibraryMaterialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PublicLibraryMaterialRepository extends JpaRepository<PublicLibraryMaterial, Long> {
    List<PublicLibraryMaterial> findAllByActiveTrueOrderByDatetimeCreatedDesc();
    List<PublicLibraryMaterial> findAllByUploadedByIdAndActiveTrueOrderByDatetimeCreatedDesc(Long uploadedById);
    List<PublicLibraryMaterial> findAllByMaterialTypeAndActiveTrueOrderByDatetimeCreatedDesc(LibraryMaterialType materialType);
    Optional<PublicLibraryMaterial> findByIdAndActiveTrue(Long id);
}
