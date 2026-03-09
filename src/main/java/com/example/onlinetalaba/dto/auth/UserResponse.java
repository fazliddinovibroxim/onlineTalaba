package com.example.onlinetalaba.dto.auth;

import com.example.onlinetalaba.enums.UserGender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private String address;
    private UserGender gender;
    private String role;
    private boolean enabled;
}
