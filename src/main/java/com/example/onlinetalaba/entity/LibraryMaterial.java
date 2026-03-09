package com.example.onlinetalaba.entity;

import com.example.onlinetalaba.enums.LibraryMaterialType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "library_materials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryMaterial extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LibraryMaterialType materialType;

    @Column(nullable = false)
    private boolean active = true;

    @OneToOne(mappedBy = "libraryMaterial", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Attachment attachment;
}