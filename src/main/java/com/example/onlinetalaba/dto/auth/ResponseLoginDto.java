package com.example.onlinetalaba.dto.auth;

import com.example.onlinetalaba.enums.AppRoleName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ResponseLoginDto {
    private String fullName;
    private String userName;
    private String email;
    private String token;
    private AppRoleName appRole;
}