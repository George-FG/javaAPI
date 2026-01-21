package main.api.dto;

public class MeResponse {
    private String username;

    public MeResponse(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}

