package com.example.onlinetalaba.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SendSmsResponse {
    private boolean success;
    private String message;
    private SendSmsResponseData data;
}
