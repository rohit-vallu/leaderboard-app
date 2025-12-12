package com.leaderboard.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "leaderboard")
public class LeaderBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(nullable = false)
    private int totalScore = 0;
}
