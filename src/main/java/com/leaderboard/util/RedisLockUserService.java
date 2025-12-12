package com.leaderboard.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisLockUserService {

    private final StringRedisTemplate redisTemplate;

    public boolean acquireLock(String key, Duration expiry) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "locked", expiry);
        return Boolean.TRUE.equals(success);
    }

    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
