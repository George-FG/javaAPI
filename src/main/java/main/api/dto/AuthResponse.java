package main.api.dto;

public class AuthResponse {

    private String username;
    private String token;

    public AuthResponse() {}

    public AuthResponse(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
