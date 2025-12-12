package com.leaderboard.repository;
import com.leaderboard.model.LeaderBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface LeaderBoardRepository extends JpaRepository<LeaderBoard, Long> {
    Optional<LeaderBoard> findByUserId(Long userId);
}