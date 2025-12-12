package com.leaderboard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "join_timestamp", nullable = false)
    private LocalDateTime joinTimestamp = LocalDateTime.now();
}
