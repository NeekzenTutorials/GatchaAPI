package com.imt.api.AuthAPI.controller;

import com.imt.api.AuthAPI.dto.*;
import com.imt.api.AuthAPI.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest req) {
    return authService.register(req);
  }

  @PostMapping("/login")
  public TokenResponse login(@Valid @RequestBody LoginRequest req) {
    String token = authService.login(req);
    return new TokenResponse(token);
  }

  // Juste pour vérifier la DB facilement
  @GetMapping("/me")
  public UserResponse me(@RequestParam String email) {
    return authService.findByEmail(email);
  }
}
