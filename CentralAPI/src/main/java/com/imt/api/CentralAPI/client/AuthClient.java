package com.imt.api.CentralAPI.client;

import com.imt.api.CentralAPI.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AuthClient {

  private final WebClient web;

  public AuthClient(@Value("${auth.base-url}") String baseUrl, WebClient.Builder builder) {
    this.web = builder.baseUrl(baseUrl).build();
  }

  public UserResponse register(RegisterRequest req) {
    return web.post().uri("/auth/register")
        .bodyValue(req)
        .retrieve()
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            resp -> resp.bodyToMono(String.class)
                .map(body -> new RuntimeException("AuthAPI error: " + resp.statusCode() + " " + body)))
        .bodyToMono(UserResponse.class)
        .block();
  }

  public TokenResponse login(LoginRequest req) {
    return web.post().uri("/auth/login")
        .bodyValue(req)
        .retrieve()
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            resp -> resp.bodyToMono(String.class)
                .map(body -> new RuntimeException("AuthAPI error: " + resp.statusCode() + " " + body)))
        .bodyToMono(TokenResponse.class)
        .block();
  }

  public ValidateResponse validate(String authorizationHeader) {
    return web.get().uri("/auth/validate")
        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
        .retrieve()
        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
            resp -> resp.bodyToMono(String.class)
                .map(body -> new RuntimeException("AuthAPI error: " + resp.statusCode() + " " + body)))
        .bodyToMono(ValidateResponse.class)
        .block();
  }
}
