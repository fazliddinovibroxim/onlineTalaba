package com.example.onlinetalaba.dto.publicview;

import com.example.onlinetalaba.enums.RoomMemberRole;
import com.example.onlinetalaba.enums.RoomVisibility;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicLibraryUserRoomResponse {
    private Long roomId;
    private String roomTitle;
    private String roomSubject;
    private RoomVisibility roomVisibility;
    private RoomMemberRole myRole;
}
