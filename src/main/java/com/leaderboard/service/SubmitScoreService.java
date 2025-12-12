package com.leaderboard.service;

import com.leaderboard.model.GameSession;
import com.leaderboard.model.LeaderBoard;
import com.leaderboard.model.User;
import com.leaderboard.repository.GameSessionRepository;
import com.leaderboard.repository.LeaderBoardRepository;
import com.leaderboard.repository.UserRepository;
import com.leaderboard.util.RedisLockUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubmitScoreService {

    private final RedisLockUserService lockService;
    private final GameSessionRepository gameSessionRepo;
    private final LeaderBoardRepository leaderboardRepo;
    private final UserRepository userRepo;
    private final RedisCacheService redisCacheService;

    private static final Duration LOCK_DURATION = Duration.ofSeconds(5);

    @Transactional
    public void submitScore(Long userId, int score) {

        String lockKey = "lock:user:" + userId;

        if (!lockService.acquireLock(lockKey, LOCK_DURATION)) {
            throw new IllegalStateException("Concurrent update detected for user " + userId);
        }

        try {
            User user = userRepo.findById(userId)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setId(userId);
                        newUser.setUsername(String.valueOf(userId));
                        newUser.setJoinTimestamp(LocalDateTime.now());
                        return userRepo.save(newUser);
                    });

            GameSession session = new GameSession();
            session.setUser(user);
            session.setScore(score);
            gameSessionRepo.save(session);

            LeaderBoard entry = leaderboardRepo.findByUserId(userId)
                    .orElseGet(() -> {
                        LeaderBoard e = new LeaderBoard();
                        e.setUser(user);
                        e.setTotalScore(0);
                        return e;
                    });

            entry.setTotalScore(entry.getTotalScore() + score);
            leaderboardRepo.save(entry);

            redisCacheService.invalidateLeaderboardCache();
            redisCacheService.invalidateUserRankCache(userId);

        } finally {
            lockService.releaseLock(lockKey);
        }
    }

}
