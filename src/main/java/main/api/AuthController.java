package main.api;

import main.api.dto.AuthResponse;
import main.api.dto.LoginRequest;
import main.api.dto.SignupRequest;
import main.user.User;
import main.user.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/signup")
  public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest req) {
    User user = userService.registerUser(req.getUsername(), req.getPassword());
    return ResponseEntity.ok(new AuthResponse(user.getUsername(), null));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
    userService.loginUser(req.getUsername(), req.getPassword());
    return ResponseEntity.ok(new AuthResponse(req.getUsername(), null));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> badRequest(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
  }
}
