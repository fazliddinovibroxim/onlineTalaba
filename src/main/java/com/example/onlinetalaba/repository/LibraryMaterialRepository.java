package com.example.onlinetalaba.repository;

import com.example.onlinetalaba.entity.LibraryMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LibraryMaterialRepository extends JpaRepository<LibraryMaterial, Long> {
    List<LibraryMaterial> findAllByRoomIdAndActiveTrue(Long roomId);
    List<LibraryMaterial> findTop10ByRoomIdInAndActiveTrueOrderByDatetimeCreatedDesc(Collection<Long> roomIds);
    List<LibraryMaterial> findTop10ByActiveTrueOrderByDatetimeCreatedDesc();
}
