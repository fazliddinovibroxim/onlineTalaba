package com.example.onlinetalaba.dto.room;

import com.example.onlinetalaba.enums.RoomVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoomRequest {
    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String subject;

    @NotNull
    private RoomVisibility visibility;

    // Optional: room yaratishda biryo'la memberlarni tanlab qo'shib qo'yish uchun.
    private List<RoomInviteRequest> members;
}
