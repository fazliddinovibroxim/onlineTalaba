package com.example.onlinetalaba.dto.publicview;

import com.example.onlinetalaba.enums.AppRoleName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PublicLibraryUploaderResponse {
    private Long userId;
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private AppRoleName role;
    private long roomCount;
    private List<PublicLibraryUserRoomResponse> rooms;
}
