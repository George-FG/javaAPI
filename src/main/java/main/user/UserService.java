package main.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public User registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters long, contain at least one special character, and at least one number"
            );
        }

        String normalizedUsername = username.trim();

        if (repo.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("User already exists");
        }

        String passwordHash = encoder.encode(password);

        User user = new User(normalizedUsername, passwordHash);
        return repo.save(user);
    }

    public boolean loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        String normalizedUsername = username.trim();

        User user = repo.findByUsername(normalizedUsername)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        boolean ok = encoder.matches(password, user.getPasswordHash());
        if (!ok) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return true;
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");
        boolean hasDigit = password.matches(".*\\d.*");
        return hasSpecial && hasDigit;
    }
}
