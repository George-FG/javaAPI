package main.api.dto;

public class LoginRequest {

    private String username;
    private String password;

    // Required for JSON deserialization
    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
