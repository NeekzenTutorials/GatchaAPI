package com.imt.api.CentralAPI.controller;

import com.imt.api.CentralAPI.client.AuthClient;
import com.imt.api.CentralAPI.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // <--- prefix BFF
public class BffAuthController {

  private final AuthClient auth;

  public BffAuthController(AuthClient auth) {
    this.auth = auth;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest req) {
    return auth.register(req);
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
