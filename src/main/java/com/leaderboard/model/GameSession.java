package com.leaderboard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int score;

    @Column(name = "game_mode", nullable = false)
    private String gameMode = "DEFAULT";

    private LocalDateTime timestamp = LocalDateTime.now();
}
