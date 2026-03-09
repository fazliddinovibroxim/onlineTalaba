package com.example.onlinetalaba.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTestApplicationRunner implements ApplicationRunner {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public void run(ApplicationArguments args) {
        try (var connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            log.info("-------------------Redis OK, value ----------------: {}", pong);
        } catch (Exception e) {
            log.warn("-------------------Redis BAD, value --------------: {}", e.getMessage());
        }
    }
}