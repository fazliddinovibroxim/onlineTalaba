package com.example.onlinetalaba.entity;

import com.example.onlinetalaba.enums.RoomMemberRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "room_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomMemberRole role;

    @Column(nullable = false)
    private boolean canManageRoom = false;

    @Column(nullable = false)
    private boolean canInviteMembers = false;

    @Column(nullable = false)
    private boolean canScheduleLesson = false;

    @Column(nullable = false)
    private boolean canUploadMaterials = false;

    @Column(nullable = false)
    private boolean active = true;
}