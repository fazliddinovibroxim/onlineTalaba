package com.example.onlinetalaba.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoomUserResponse {
    private Long id;
    private String roomTitle;
    private String subject;
    private Boolean active;
}
