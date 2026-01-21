package main.api;

import main.api.dto.AuthResponse;
import main.api.dto.LoginRequest;
import main.api.dto.SignupRequest;
import main.user.AuthService;
import main.user.User;
import main.user.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest req, HttpServletResponse response) {
    User user = userService.registerUser(req.getUsername(), req.getPassword());
    
    String sessionToken = authService.createSession(user, 30 * 60);
    String refreshToken = authService.createRefreshToken(user, 14 * 24 * 60 * 60);

    Cookie sessionCookie = new Cookie("SESSION", sessionToken);
    sessionCookie.setHttpOnly(true);
    sessionCookie.setSecure(true);
    sessionCookie.setPath("/");
    sessionCookie.setMaxAge(30 * 60); // 30 minutes
    sessionCookie.setDomain(".george.richmnd.uk");

    Cookie refreshCookie = new Cookie("REFRESH", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(true);
    refreshCookie.setPath("/auth/refresh"); // only sent to refresh endpoint
    refreshCookie.setMaxAge(14 * 24 * 60 * 60); // 14 days
    refreshCookie.setDomain("auth.george.richmnd.uk");

    response.addCookie(sessionCookie);
    response.addCookie(refreshCookie);
    
    return ResponseEntity.ok(new AuthResponse(user.getUsername(), null));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req, HttpServletResponse response) {
    userService.loginUser(req.getUsername(), req.getPassword());
    
    User user = userService.getUserByUsername(req.getUsername());
    
    String sessionToken = authService.createSession(user, 30 * 60);
    String refreshToken = authService.createRefreshToken(user, 14 * 24 * 60 * 60);

    Cookie sessionCookie = new Cookie("SESSION", sessionToken);
    sessionCookie.setHttpOnly(true);
    sessionCookie.setSecure(true);
    sessionCookie.setPath("/");
    sessionCookie.setMaxAge(30 * 60); // 30 minutes
    sessionCookie.setDomain(".george.richmnd.uk");
   
    Cookie refreshCookie = new Cookie("REFRESH", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(true);
    refreshCookie.setPath("/auth/refresh"); // only sent to refresh endpoint
    refreshCookie.setMaxAge(14 * 24 * 60 * 60); // 14 days
    refreshCookie.setDomain("auth.george.richmnd.uk");

    response.addCookie(sessionCookie);
    response.addCookie(refreshCookie);
    
    return ResponseEntity.ok(new AuthResponse(req.getUsername(), null));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> badRequest(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }
}
