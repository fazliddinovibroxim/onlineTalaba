package com.example.onlinetalaba.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment extends BaseUUIDEntity {

    @Column(nullable = false)
    private String serverName;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false, updatable = false)
    private long size;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_material_id")
    private LibraryMaterial libraryMaterial;
}