package main.leaderboard;

import jakarta.persistence.*;

@Entity
@Table(name = "scores",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "game"})
    }
)
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = false)
    private String username;

    @Column(nullable = false, unique = false)
    private String game;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private Long timestamp;

    // JPA requires a no-args constructor
    protected Score() {}

    public Score(String username, String game, int score) {
        this.username = username;
        this.game = game;
        this.score = score;
        this.timestamp = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getGame() {
        return game;
    }

    public int getScore() {
        return score;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void updateScore(int score) {
        this.score = score;
    }

    public void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

}
