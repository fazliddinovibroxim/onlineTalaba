package com.example.onlinetalaba.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SendSmsResponseData {
    private Long sms_id;
    private String request_id;
    private String status;
    private Integer parts_count;
    private Integer total_cost;
    private Integer balance;
}
