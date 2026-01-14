package com.imt.api.AuthAPI.service;

import com.imt.api.AuthAPI.dto.*;
import com.imt.api.AuthAPI.model.UserAccount;
import com.imt.api.AuthAPI.repository.UserAccountRepository;
import com.imt.api.AuthAPI.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private final UserAccountRepository repo;
  private final PasswordEncoder encoder;
  private final JwtUtil jwt;

  public AuthService(UserAccountRepository repo, PasswordEncoder encoder, JwtUtil jwt) {
    this.repo = repo;
    this.encoder = encoder;
    this.jwt = jwt;
  }

  public UserResponse register(RegisterRequest req) {
    if (repo.existsByEmail(req.email())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already used");
    }
    if (repo.existsByPseudo(req.pseudo())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Pseudo already used");
    }

    var user = new UserAccount(req.email(), req.pseudo(), encoder.encode(req.password()));
    var saved = repo.save(user);

    return new UserResponse(saved.getId(), saved.getEmail(), saved.getCreatedAt());
  }

  public String login(LoginRequest req) {
    var user = repo.findByEmail(req.email())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));

    if (!encoder.matches(req.password(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
    }

    return jwt.generateToken(user.getPseudo());
  }

  /** Sliding expiration: si valide, renvoie pseudo + un nouveau token valable 1h à partir de maintenant */
  public ValidateResponse validateAndRefresh(String authHeader) {
    String token = extractBearer(authHeader);

    if (!jwt.validateJwtToken(token)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
    }

    String pseudo = jwt.getPseudoFromToken(token);
    String newToken = jwt.generateToken(pseudo);
    return new ValidateResponse(pseudo, newToken);
  }

  public UserResponse findByEmail(String email) {
    var user = repo.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
  }

  private String extractBearer(String header) {
    if (header == null || !header.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token");
    }
    return header.substring(7);
  }
}
