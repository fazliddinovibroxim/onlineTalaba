package com.example.onlinetalaba;

import com.example.onlinetalaba.config.LiveKitProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(LiveKitProps.class)
@SpringBootApplication
public class OnlineTalabaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineTalabaApplication.class, args);
    }

}
