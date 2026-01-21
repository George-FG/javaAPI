package main.leaderboard;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ScoreService {

    private final ScoresRepository repo;

    public ScoreService(ScoresRepository repo) {
        this.repo = repo;
    }

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

        Score scoreObj = new Score(username.trim(), game.trim(), score);

        return repo.save(scoreObj);
    }

    public Page<Score> getScoresByGame(String game, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return repo.findByGameOrderByScoreDesc(game, pageRequest);
    }
}

