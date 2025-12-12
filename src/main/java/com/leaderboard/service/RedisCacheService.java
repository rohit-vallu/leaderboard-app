package com.leaderboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LEADERBOARD_CACHE_KEY = "leaderboard:top10";

    public List<Object[]> getTop10() {
        try {
            String json = redis.opsForValue().get(LEADERBOARD_CACHE_KEY);
            if (json == null) return null;

            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void cacheTop10(List<Object[]> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redis.opsForValue().set(LEADERBOARD_CACHE_KEY, json, Duration.ofSeconds(5));
        } catch (Exception e) {
            throw new RuntimeException("Failed to cache leaderboard", e);
        }
    }

    public void invalidateLeaderboardCache() {
        redis.delete(LEADERBOARD_CACHE_KEY);
    }

    public String getUserRank(Long userId) {
        return redis.opsForValue().get("leaderboard:rank:" + userId);
    }

    public void cacheUserRank(Long userId, int rank) {
        redis.opsForValue()
                .set("leaderboard:rank:" + userId, String.valueOf(rank), Duration.ofSeconds(5));
    }

    public void invalidateUserRankCache(Long userId) {
        redis.delete("leaderboard:rank:" + userId);
    }


}
