package com.example.onlinetalaba.dto.auth;

import com.example.onlinetalaba.enums.AppRoleName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ResponseLoginDto {
    private String username;
    private String email;
    private String token;
    private AppRoleName appRole;
}