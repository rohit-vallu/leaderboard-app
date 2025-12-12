package com.leaderboard.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderBoardQueryService {

    private final EntityManager em;
    private final RedisCacheService redisCache;

    public List<Object[]> getTop10() {

        List<Object[]> cached = redisCache.getTop10();
        if (cached != null) return cached;

        String sql = """
            SELECT l.user_id, u.username, l.total_score,
                ROW_NUMBER() OVER (
                    ORDER BY l.total_score DESC, u.join_timestamp ASC, u.id ASC
                ) AS rank
            FROM leaderboard l
            JOIN users u ON l.user_id = u.id
            LIMIT 10
            """;

        Query query = em.createNativeQuery(sql);
        List<Object[]> result = query.getResultList();

        redisCache.cacheTop10(result);

        return result;
    }

    public int getPlayerRank(Long userId) {


        String cachedRank = redisCache.getUserRank(userId);
        if (cachedRank != null) {
            return Integer.parseInt(cachedRank);
        }


        String sql = """
        SELECT rank FROM (
            SELECT l.user_id,
                   ROW_NUMBER() OVER (
                       ORDER BY l.total_score DESC,
                                u.join_timestamp ASC,
                                u.id ASC
                   ) AS rank
            FROM leaderboard l
            JOIN users u ON l.user_id = u.id
        ) ranked
        WHERE ranked.user_id = :userId
        """;

        Query query = em.createNativeQuery(sql);
        query.setParameter("userId", userId);

        Object result = query.getSingleResult();
        int rank = ((Number) result).intValue();

        redisCache.cacheUserRank(userId, rank);

        return rank;
    }

}
