package com.example.onlinetalaba.dto.publicview;

import com.example.onlinetalaba.enums.RoomVisibility;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PublicRoomRichResponse {
    private Long roomId;
    private String title;
    private String subject;
    private String description;
    private RoomVisibility visibility;
    private boolean active;

    private Long ownerId;
    private String ownerName;

    private long memberCount;
    private long teacherCount;
    private long activeLessonCount;
    private long weeklyLessonCount;
    private long resourceCount;

    private LocalDateTime lastLessonAt;
    private LocalDateTime lastMaterialAt;
    private LocalDateTime lastActiveAt;
    private boolean liveNow;

    private List<PublicLessonMiniResponse> upcomingLessons;
    private List<PublicMaterialMiniResponse> recentMaterials;

    private List<String> outcomes;
    private String level;
    private List<String> prerequisites;
    private List<String> tags;
    private String language;
    private String priceType;

    private boolean canJoinDirectly;
    private boolean requiresApproval;
    private LocalDateTime nextIntakeDate;

    private long joinCount30d;
    private Long reviewsCount;
    private Double completionRate;
}
