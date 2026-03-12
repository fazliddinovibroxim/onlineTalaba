package com.example.onlinetalaba.util;

import com.example.onlinetalaba.dto.sms.SendSmsRequest;
import com.example.onlinetalaba.dto.sms.SendSmsResponse;
import com.example.onlinetalaba.dto.sms.SmsApiBalanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${spring.devsms.token}")
    private String token;

    private final String sendSmsBaseUrl = "https://devsms.uz/api";

    String template = "yasinmebel.uz saytidan ro'yhatdan o'tish uchun kod : ";

    private final WebClient webClient;


    public boolean sendSms(String phone, String code) {
        SendSmsRequest sendSmsRequest = new SendSmsRequest(
                phone,
                template + code,
                "4546" // from (ixtiyoriy)
        );

        SendSmsResponse response = webClient.post()
                .uri(sendSmsBaseUrl + "/send_sms.php")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(sendSmsRequest)
                .retrieve()
                .bodyToMono(SendSmsResponse.class)
                .onErrorResume(throwable -> {
                    throwable.printStackTrace();
                    return Mono.empty();
                })
                .block();

        System.out.println("send sms response: " + response);
        return response != null && response.isSuccess();
    }

    public Mono<SmsApiBalanceResponse> smsApiBalance() {
        return webClient.post()
                .uri(sendSmsBaseUrl + "/get_balance.php")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(SmsApiBalanceResponse.class)
                .onErrorResume(throwable -> {
                    throwable.printStackTrace();
                    return Mono.empty();
                });
    }
}

