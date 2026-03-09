package com.example.onlinetalaba.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.livekit")
public record LiveKitProps(
        String url,
        String apiKey,
        String apiSecret
) {}