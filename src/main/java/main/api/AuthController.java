package main.api;

import main.api.dto.AuthResponse;
import main.api.dto.LoginRequest;
import main.api.dto.MeResponse;
import main.api.dto.SignupRequest;
import main.user.AuthService;
import main.user.User;
import main.user.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

  private final UserService userService;
  private final AuthService authService;

  public AuthController(UserService userService, AuthService authService) {
    this.userService = userService;
    this.authService = authService;
  }

  private Cookie[] createAuthCookies(String sessionToken, String refreshToken) {
    Cookie sessionCookie = new Cookie("SESSION", sessionToken);
    sessionCookie.setHttpOnly(true);
    sessionCookie.setPath("/");
    sessionCookie.setMaxAge(1 * 60); // 1 minutes
   
    Cookie refreshCookie = new Cookie("REFRESH", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/"); 
    refreshCookie.setMaxAge(2 * 60); // 2 minutes

    return new Cookie[] { sessionCookie, refreshCookie };
  }

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest req, HttpServletResponse response) {
    User user = userService.registerUser(req.getUsername(), req.getPassword());
    
    String sessionToken = authService.createSession(user, 1 * 60);
    String refreshToken = authService.createRefreshToken(user, 2 * 60);

    Cookie[] cookies = createAuthCookies(sessionToken, refreshToken);
    for (Cookie cookie : cookies) {
      response.addCookie(cookie);
    }
    
    return ResponseEntity.ok(new AuthResponse(user.getUsername(), null));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
    userService.loginUser(req.getUsername(), req.getPassword());
    
    User user = userService.getUserByUsername(req.getUsername());
    
    String sessionToken = authService.createSession(user, 1 * 60);
    String refreshToken = authService.createRefreshToken(user, 2 * 60);

    Cookie[] cookies = createAuthCookies(sessionToken, refreshToken);
    for (Cookie cookie : cookies) {
      response.addCookie(cookie);
    }
    
    return ResponseEntity.ok(new AuthResponse(req.getUsername(), null));
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(@CookieValue(value = "SESSION", required = false) String sessionToken) {

      if (sessionToken == null) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      String username = authService.findUserBySession(sessionToken);
      if (username == null) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      return ResponseEntity.ok(new MeResponse(username));
  }

  @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(value = "REFRESH", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authService.findUserByRefreshToken(refreshToken);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String sessionToken = authService.createSession(userService.getUserByUsername(username), 1 * 60);
        String newRefreshToken = authService.createRefreshToken(userService.getUserByUsername(username), 2 * 60);

        Cookie[] cookies = createAuthCookies(sessionToken, newRefreshToken);
        for (Cookie cookie : cookies) {
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(Collections.singletonMap("message", "Tokens refreshed successfully"));
    }



  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> badRequest(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }
}