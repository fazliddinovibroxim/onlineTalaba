package com.example.onlinetalaba.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDirectoryItemResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String address;
}

