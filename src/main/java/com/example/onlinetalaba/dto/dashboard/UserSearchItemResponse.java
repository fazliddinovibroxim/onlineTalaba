package com.example.onlinetalaba.dto.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSearchItemResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
}
