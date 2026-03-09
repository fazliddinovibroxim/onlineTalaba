package com.example.onlinetalaba.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmsApiBalanceResponseDataStatistics {
    private String total_sms;
    private String total_spent;
    private String today_sms;
    private String today_spent;
    private String month_sms;
    private String month_spent;
}
