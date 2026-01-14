package com.imt.api.AuthAPI.service;

import com.imt.api.AuthAPI.dto.LoginRequest;
import com.imt.api.AuthAPI.dto.RegisterRequest;
import com.imt.api.AuthAPI.dto.UserResponse;
import com.imt.api.AuthAPI.model.UserAccount;
import com.imt.api.AuthAPI.repository.UserAccountRepository;
import com.imt.api.AuthAPI.util.Passwords;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

  private final UserAccountRepository repo;

  public AuthService(UserAccountRepository repo) {
    this.repo = repo;
  }

  public UserResponse register(RegisterRequest req) {
    if (repo.existsByEmail(req.email())) {
      throw new IllegalArgumentException("Email already used");
    }

    var user = new UserAccount(req.email(), Passwords.hash(req.password()));
    var saved = repo.save(user);

    return new UserResponse(saved.getId(), saved.getEmail(), saved.getCreatedAt());
  }

  // Pour test simple: token fake (UUID). Plus tard tu mettras un vrai JWT.
  public String login(LoginRequest req) {
    var user = repo.findByEmail(req.email())
        .orElseThrow(() -> new IllegalArgumentException("Bad credentials"));

    if (!Passwords.matches(req.password(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Bad credentials");
    }

    return UUID.randomUUID().toString();
  }

  public UserResponse findByEmail(String email) {
    var user = repo.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
  }
}
