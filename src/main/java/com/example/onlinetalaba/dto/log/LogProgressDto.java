package com.example.onlinetalaba.dto.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogProgressDto {
    private String action;
    private String type;
    private String exception;
}
