package com.example.onlinetalaba.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SendSmsRequest {
    private String phone;
    private String message;
    private String from;
}