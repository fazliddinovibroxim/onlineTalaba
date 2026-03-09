package com.example.onlinetalaba.dto.room;

import com.example.onlinetalaba.enums.RoomVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomRequest {
    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String subject;

    @NotNull
    private RoomVisibility visibility;
}