package main.leaderboard;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreService {

    private final ScoresRepository repo;

    public ScoreService(ScoresRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Score registerScore(String username, String game, int score) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (game == null || game.trim().isEmpty()) {
            throw new IllegalArgumentException("Game cannot be null or empty");
        }
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        Optional<Score> existing = repo.findByUsernameAndGame(username, game);

        if (existing.isEmpty()) {
            return repo.save(new Score(username, game, score));
        }

        Score current = existing.get();

        if (game.equals("pathfinding") || game.equals("sorting")) {
                current.updateScore(score + current.getScore());
                current.updateTimestamp();
        }
        else if (score > current.getScore()) {
            current.updateScore(score);
            current.updateTimestamp();
        }

        return null;
    }

    public Page<Score> getScoresByGame(String game, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return repo.findByGameOrderByScoreDesc(game, pageRequest);
    }
}

