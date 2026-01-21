package main.api.dto;

public class ScoreRequest {
    private String game;
    private int score;

    public ScoreRequest() {}

    public ScoreRequest(String game, int score) {
        this.game = game;
        this.score = score;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}