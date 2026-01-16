package com.imt.api.CentralAPI.controller;

import com.imt.api.CentralAPI.client.AuthClient;
import com.imt.api.CentralAPI.client.PlayerClient;
import com.imt.api.CentralAPI.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth (BFF)", description = "Routes d'authentification exposées au front via CentralAPI")
@RestController
@RequestMapping("/api/auth")
public class BffAuthController {

  private final AuthClient auth;
  private final PlayerClient player;

  public BffAuthController(AuthClient auth, PlayerClient player) {
    this.auth = auth;
    this.player = player;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest req) {
    var user = auth.register(req);

    try {
      player.create(new CreatePlayerRequest(req.pseudo()));
    } catch (RuntimeException ex) {
      System.err.println("Player creation failed for pseudo=" + req.pseudo() + " : " + ex.getMessage());
    }

    return user;
  }

  @PostMapping("/login")
  public TokenResponse login(@Valid @RequestBody LoginRequest req) {
    return auth.login(req);
  }

  @GetMapping("/validate")
  public ValidateResponse validate(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    return auth.validate(authorization);
  }
}
