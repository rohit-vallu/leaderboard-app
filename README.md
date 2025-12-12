# 🏆 Gaming Leaderboard System — High Performance + Low Latency

A scalable, real-time gaming leaderboard system supporting:

- Score submissions
- Real-time leaderboard updates
- Player rank lookup
- Redis caching (low latency)
- Redis distributed locking (atomicity & concurrency)
- PostgreSQL window functions (ranking)
- New Relic integration (monitoring + performance analysis)
- Responsive HTML/CSS/JS frontend with polling

---

# 📌 **1. High Level Design (HLD)**

## 🎯 System Goals
- Handle **high throughput** score submissions.
- Maintain **accurate, real-time leaderboard** rankings.
- Ensure **atomicity** and **consistency** under concurrent writes.
- Provide **low-latency** APIs that scale to **1 million+ users**.
- Offer a **responsive, auto-refreshing UI**.
- Monitor performance using **New Relic APM**.

---

## 🏗 **Architecture Overview**

                      +-----------------------------+
                      |       HTML / JS Frontend    |
                      |  (Polling every 3 seconds)   |
                      +------------+-----------------+
                                   |
                          GET /top | GET /rank | POST /submit
                                   |
                      +------------v-----------------+
                      |      Spring Boot Backend     |
                      | Controllers | Services       |
                      +------------+-----------------+
                                   |
                  +----------------+-----------------------+
                  |                                        |
    +-------------v-------------+           +--------------v-------------+
    |    Redis Cache + Locking  |           |     PostgreSQL Database    |
    |  - leaderboard:top10      |           |  - users                   |
    |  - leaderboard:rank:<id>  |           |  - game_sessions           |
    |  - lock:user:<id>         |           |  - leaderboard (totals)    |
    +---------------------------+           +-----------------------------+

---

## 🚀 **Key High-Level Concepts**

### 🧠 Caching
- Leaderboard cached under: `leaderboard:top10`
- Player rank cached under: `leaderboard:rank:<userId>`
- Cache invalidated on score submission
- Read-through strategy → cache rebuilt on demand

### 🔐 Concurrency
- Per-user Redis distributed lock using `SETNX`
- Ensures atomic writes + prevents race conditions

### 📊 Ranking Algorithm
Using PostgreSQL window function:

```sql
ROW_NUMBER() OVER (
    ORDER BY total_score DESC,
             join_timestamp ASC,
             user_id ASC
)
```

#### Ensures:
* Unique rank
* Tie-breaking by timestamp
* Deterministic ordering

#### 📈 Monitoring — New Relic
* Tracks API latency
* Tracks DB and Redis timings
* Shows slow transactions
* Alerts on high response times

# 📌 **2. Low Level Design (LLD)**
```
src/main/java/com/leaderboard
│
├── controller
│   └── LeaderboardController.java
│
├── service
│   ├── ScoreService.java
│   ├── LeaderboardQueryService.java
│   ├── RedisCacheService.java
│   └── RedisLockService.java
│
├── model
│   ├── User.java
│   ├── GameSession.java
│   └── LeaderboardEntry.java
│
├── repository
│   ├── UserRepository.java
│   ├── GameSessionRepository.java
│   └── LeaderboardRepository.java
│
└── LeaderboardApplication.java
```

### 🔐 Redis Locking
```
lock:user:<userId> → "locked" (TTL = 5 seconds)
```
* Request A and B hit userId=10.
* Only A acquires lock.
* B waits or fails.
* A updates DB → invalidates Redis → releases lock.
* B retries or fails gracefully.


### Redis Caching
**Cached keys**
```
leaderboard:top10
leaderboard:rank:<userId>
```

**Invalidation rules**
* Score submission → delete top10 and rank:userId
* TTL ensures self-refresh after expiry


# 🏆 **Leaderboard Fetch Workflow**
```
SequenceDiagram
    Client->>Backend: GET /top
    Backend->>Redis: GET leaderboard:top10
    alt Cache hit
        Redis-->>Backend: Return list
        Backend-->>Client: Cached leaderboard
    else Cache miss
        Backend->>DB: Execute ROW_NUMBER() ranking query
        Backend->>Redis: SET leaderboard:top10 (TTL 5s)
        Backend-->>Client: Fresh leaderboard
    end
```

