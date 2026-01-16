package com.imt.api.CentralAPI.controller;

import com.imt.api.CentralAPI.client.PlayerClient;
import com.imt.api.CentralAPI.config.OpenApiConfig;
import com.imt.api.CentralAPI.dto.*;
import com.imt.api.CentralAPI.service.AuthGateway;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Player (BFF)", description = "Profil joueur exposé au front via CentralAPI (token -> pseudo)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/player")
public class BffPlayerController {

  private final AuthGateway authGateway;
  private final PlayerClient player;

  public BffPlayerController(AuthGateway authGateway, PlayerClient player) {
    this.authGateway = authGateway;
    this.player = player;
  }

  @GetMapping("/profile")
  public PlayerResponse profile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    var v = authGateway.validate(authorization);
    return player.profile(v.pseudo());
  }

  @GetMapping("/monsters")
  public MonstersResponse monsters(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    var v = authGateway.validate(authorization);
    return player.monsters(v.pseudo());
  }

  @GetMapping("/level")
  public LevelResponse level(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    var v = authGateway.validate(authorization);
    return player.level(v.pseudo());
  }

  @PostMapping("/xp")
  public PlayerResponse gainXp(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @Valid @RequestBody GainXpRequest req
  ) {
    var v = authGateway.validate(authorization);
    return player.gainXp(v.pseudo(), req);
  }

  @PostMapping("/level-up")
  public PlayerResponse levelUp(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    var v = authGateway.validate(authorization);
    return player.levelUp(v.pseudo());
  }

  @PostMapping("/monsters")
  public PlayerResponse acquireMonster(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @Valid @RequestBody AcquireMonsterRequest req
  ) {
    var v = authGateway.validate(authorization);
    return player.acquireMonster(v.pseudo(), req);
  }

  @DeleteMapping("/monsters/{monsterId}")
  public PlayerResponse removeMonster(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @PathVariable String monsterId
  ) {
    var v = authGateway.validate(authorization);
    return player.removeMonster(v.pseudo(), monsterId);
  }
}
