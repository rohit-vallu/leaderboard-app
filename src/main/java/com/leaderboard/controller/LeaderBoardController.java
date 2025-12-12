package com.leaderboard.controller;

import com.leaderboard.service.LeaderBoardQueryService;
import com.leaderboard.service.SubmitScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderBoardController {

    private final SubmitScoreService scoreService;
    private final LeaderBoardQueryService queryService;

    @PostMapping("/submit")
    public String submitScore(@RequestParam Long userId, @RequestParam int score) {
        scoreService.submitScore(userId, score);
        return "Score updated successfully.";
    }

    @GetMapping("/top")
    public Object getTop10() {
        return queryService.getTop10();
    }

    @GetMapping("/rank/{userId}")
    public Object getPlayerRank(@PathVariable Long userId) {
        int rank = queryService.getPlayerRank(userId);
        return Map.of("userId", userId, "rank", rank);
    }

}
