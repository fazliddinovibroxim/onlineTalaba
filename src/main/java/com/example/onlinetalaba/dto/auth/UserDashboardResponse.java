package com.example.onlinetalaba.dto.auth;

import com.example.onlinetalaba.enums.AppPermissions;
import com.example.onlinetalaba.enums.AppRoleName;
import com.example.onlinetalaba.enums.UserGender;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserDashboardResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;

    private AppRoleName role;
    private UserGender gender;
    private Set<AppPermissions> permissions;

    private List<RoomUserResponse> orders;
}
