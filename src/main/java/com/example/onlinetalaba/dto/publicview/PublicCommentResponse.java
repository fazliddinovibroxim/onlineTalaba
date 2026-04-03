package com.example.onlinetalaba.dto.publicview;

import com.example.onlinetalaba.enums.AppRoleName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PublicCommentResponse {
    private Long id;
    private Long parentCommentId;
    private Long senderId;
    private String senderName;
    private AppRoleName senderRole;
    private String content;
    private boolean question;
    private boolean resolved;
    private boolean edited;
    private long replyCount;
    private boolean canEdit;
    private boolean canDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<PublicCommentResponse> replies = new ArrayList<>();
}
