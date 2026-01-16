package com.imt.api.CentralAPI.service;

import com.imt.api.CentralAPI.client.AuthClient;
import com.imt.api.CentralAPI.dto.ValidateResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthGateway {

  private final AuthClient auth;

  public AuthGateway(AuthClient auth) {
    this.auth = auth;
  }

  public ValidateResponse validate(String authorizationHeader) {
    if (authorizationHeader == null || authorizationHeader.isBlank()) {
      throw new IllegalArgumentException("Missing Authorization header");
    }
    return auth.validate(authorizationHeader);
  }

  public static String bearer(String token) {
    return "Bearer " + token;
  }
}
