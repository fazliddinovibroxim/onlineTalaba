package com.example.onlinetalaba.dto.publicview;

import lombok.Data;

@Data
public class PublicCommentCreateRequest {
    private String content;
    private Long parentCommentId;
    private Boolean question;
}
