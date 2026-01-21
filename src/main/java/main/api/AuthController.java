package main.api;

import main.api.dto.AuthResponse;
import main.api.dto.LoginRequest;
import main.api.dto.MeResponse;
import main.api.dto.ScoreRequest;
import main.api.dto.SignupRequest;
import main.leaderboard.ScoreService;
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
  private final ScoreService scoreService;

  public AuthController(UserService userService, AuthService authService, ScoreService scoreService) {
    this.userService = userService;
    this.authService = authService;
    this.scoreService = scoreService;
  }

  private Cookie[] createAuthCookies(String sessionToken, String refreshToken) {
    Cookie sessionCookie = new Cookie("SESSION", sessionToken);
    sessionCookie.setMaxAge(10 * 60); // 10 minutes
    sessionCookie.setSecure(true);
    sessionCookie.setPath("/");
    sessionCookie.setDomain("george.richmond.gg");
    sessionCookie.setAttribute("SameSite", "None");
   
    Cookie refreshCookie = new Cookie("REFRESH", refreshToken);
    refreshCookie.setMaxAge(20 * 60); // 20 minutes
    refreshCookie.setSecure(true);
    refreshCookie.setPath("/");
    refreshCookie.setDomain("george.richmond.gg");
    refreshCookie.setAttribute("SameSite", "None");


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
    
    response.setHeader("Access-Control-Allow-Credentials", "true");
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
    
    response.setHeader("Access-Control-Allow-Credentials", "true");
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

        response.setHeader("Access-Control-Allow-Credentials", "true");
        return ResponseEntity.ok(Collections.singletonMap("message", "Tokens refreshed successfully"));
    }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(
      @CookieValue(value = "SESSION", required = false) String sessionToken,
      @CookieValue(value = "REFRESH", required = false) String refreshToken,
      HttpServletResponse response) {

      if (sessionToken != null) {
          authService.invalidateSession(sessionToken);
      }
      if (refreshToken != null) {
          authService.invalidateRefreshToken(refreshToken);
      }

      Cookie sessionCookie = new Cookie("SESSION", null);
      sessionCookie.setMaxAge(0);
      sessionCookie.setSecure(true);
      sessionCookie.setPath("/");
      sessionCookie.setDomain("george.richmond.gg");
      sessionCookie.setAttribute("SameSite", "None");

      Cookie refreshCookie = new Cookie("REFRESH", null);
      refreshCookie.setMaxAge(0);
      refreshCookie.setSecure(true);
      refreshCookie.setPath("/");
      refreshCookie.setDomain("george.richmond.gg");
      refreshCookie.setAttribute("SameSite", "None");

      response.addCookie(sessionCookie);
      response.addCookie(refreshCookie);
      response.setHeader("Access-Control-Allow-Credentials", "true");
      return ResponseEntity.ok(Collections.singletonMap("message", "Logged out successfully"));
  }



  @PostMapping("/submit-score")
  public ResponseEntity<?> submitScore(@CookieValue(value = "SESSION", required = false) String sessionToken,
                                     @RequestBody ScoreRequest req,
                                    HttpServletResponse response) {

      if (sessionToken == null) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      String username = authService.findUserBySession(sessionToken);
      if (username == null) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      try {
        scoreService.registerScore(username, req.getGame(), req.getScore());
      } catch (Exception e) {
        System.out.println("Error submitting score: " + e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
      }
      response.setHeader("Access-Control-Allow-Credentials", "true");
      return ResponseEntity.ok(Collections.singletonMap("message", "Score submitted successfully"));
  }

  @GetMapping("/scores-by-game")
  public ResponseEntity<?> getScores(@RequestParam String game,
                                     @RequestParam int page,
                                     @RequestParam int size) {
      return ResponseEntity.ok(scoreService.getScoresByGame(game, page, size));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> badRequest(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }
}