package com.imt.api.CentralAPI.client;

import com.imt.api.CentralAPI.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PlayerClient {

  private final WebClient web;

  public PlayerClient(@Value("${player.base-url}") String baseUrl, WebClient.Builder builder) {
    this.web = builder.baseUrl(baseUrl).build();
  }

  public PlayerResponse create(CreatePlayerRequest req) {
    return web.post().uri("/players")
        .bodyValue(req)
        .retrieve()
        .bodyToMono(PlayerResponse.class)
        .block();
  }

  public PlayerResponse profile(String pseudo) {
    return web.get().uri("/players/{pseudo}", pseudo)
        .retrieve()
        .bodyToMono(PlayerResponse.class)
        .block();
  }

  public MonstersResponse monsters(String pseudo) {
    return web.get().uri("/players/{pseudo}/monsters", pseudo)
        .retrieve()
        .bodyToMono(MonstersResponse.class)
        .block();
  }

  public LevelResponse level(String pseudo) {
    return web.get().uri("/players/{pseudo}/level", pseudo)
        .retrieve()
        .bodyToMono(LevelResponse.class)
        .block();
  }

  public PlayerResponse gainXp(String pseudo, GainXpRequest req) {
    return web.post().uri("/players/{pseudo}/xp", pseudo)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(PlayerResponse.class)
        .block();
  }

  public PlayerResponse levelUp(String pseudo) {
    return web.post().uri("/players/{pseudo}/level-up", pseudo)
        .retrieve()
        .bodyToMono(PlayerResponse.class)
        .block();
  }

  public PlayerResponse acquireMonster(String pseudo, AcquireMonsterRequest req) {
    return web.post().uri("/players/{pseudo}/monsters", pseudo)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(PlayerResponse.class)
        .block();
  }

  public PlayerResponse removeMonster(String pseudo, String monsterId) {
    return web.delete().uri("/players/{pseudo}/monsters/{monsterId}", pseudo, monsterId)
        .retrieve()
        .bodyToMono(PlayerResponse.class)
        .block();
  }
}
