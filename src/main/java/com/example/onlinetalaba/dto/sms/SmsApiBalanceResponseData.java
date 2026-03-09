package com.example.onlinetalaba.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmsApiBalanceResponseData {
    private String balance;
    private String sms_price;
    private SmsApiBalanceResponseDataStatistics statistics;

}
