package com.example.onlinetalaba.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.livekit")
public record LiveKitProps(
        String url,
        String apiUrl,
        String wsUrl,
        String apiKey,
        String apiSecret
) {
    public String apiBaseUrl() {
        return apiUrl != null && !apiUrl.isBlank() ? apiUrl : url;
    }

    public String clientUrl() {
        return wsUrl != null && !wsUrl.isBlank() ? wsUrl : url;
    }
}
