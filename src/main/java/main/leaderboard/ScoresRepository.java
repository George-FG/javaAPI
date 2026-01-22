package main.leaderboard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ScoresRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByUsername(String username);
    Optional<Score> findByUsernameAndGame(String username, String game);
    boolean findByScore(int score);
    
    Page<Score> findByGameOrderByScoreDesc(String game, Pageable pageable);
    Page<Score> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);
    
}
