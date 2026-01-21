package main.user;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final Map<String, TokenData> sessionTokens = new ConcurrentHashMap<>();
    private final Map<String, TokenData> refreshTokens = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public String createSession(User user, int durationSeconds) {
        String token = generateToken();
        long expiryTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        sessionTokens.put(token, new TokenData(user.getUsername(), expiryTime));
        return token;
    }

    public String createRefreshToken(User user, int durationSeconds) {
        String token = generateToken();
        long expiryTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        refreshTokens.put(token, new TokenData(user.getUsername(), expiryTime));
        return token;
    }

    public String findUserBySession(String token) {
        TokenData data = sessionTokens.get(token);
        if (data == null || data.getExpiryTime() < System.currentTimeMillis()) {
            return null;
        }
        return data.getUsername();
    }

    public String findUserByRefreshToken(String token) {
        TokenData data = refreshTokens.get(token);
        if (data == null || data.getExpiryTime() < System.currentTimeMillis()) {
            return null;
        }
        return data.getUsername();
    }

    public void invalidateSession(String token) {
        sessionTokens.remove(token);
    }

    public void invalidateRefreshToken(String token) {
        refreshTokens.remove(token);
    }

    private String generateToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private static class TokenData {
        private final String username;
        private final long expiryTime;

        public TokenData(String username, long expiryTime) {
            this.username = username;
            this.expiryTime = expiryTime;
        }

        public String getUsername() {
            return username;
        }

        public long getExpiryTime() {
            return expiryTime;
        }
    }
}
