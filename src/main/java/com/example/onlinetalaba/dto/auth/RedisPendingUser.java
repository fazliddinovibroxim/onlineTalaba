package com.example.onlinetalaba.dto.auth;

import com.example.onlinetalaba.enums.AuthRoleName;
import com.example.onlinetalaba.enums.UserGender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisPendingUser {
    private String username;
    private String email;
    private String password;
    private String emailCode;
    private UserGender userGender;
    private AuthRoleName role;
}
