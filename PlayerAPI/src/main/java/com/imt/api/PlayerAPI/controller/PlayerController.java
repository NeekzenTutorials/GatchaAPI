package com.imt.api.PlayerAPI.controller;

import com.imt.api.PlayerAPI.dto.*;
import com.imt.api.PlayerAPI.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
public class PlayerController {

  private final PlayerService service;

  public PlayerController(PlayerService service) {
    this.service = service;
  }

  // création (appelée par le BFF juste après register)
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PlayerResponse create(@Valid @RequestBody CreatePlayerRequest req) {
    return service.create(req.pseudo());
  }

  @GetMapping("/{pseudo}")
  public PlayerResponse profile(@PathVariable String pseudo) {
    return service.getProfile(pseudo);
  }

  @GetMapping("/{pseudo}/monsters")
  public MonstersResponse monsters(@PathVariable String pseudo) {
    var p = service.getProfile(pseudo);
    return new MonstersResponse(p.maxMonsters(), p.monsters());
  }

  @GetMapping("/{pseudo}/level")
  public LevelResponse level(@PathVariable String pseudo) {
    var p = service.getProfile(pseudo);
    return new LevelResponse(p.level(), p.experience(), p.nextLevelXp(), p.canLevelUp());
  }

  @PostMapping("/{pseudo}/xp")
  public PlayerResponse gainXp(@PathVariable String pseudo, @Valid @RequestBody GainXpRequest req) {
    return service.gainXp(pseudo, req.amount());
  }

  @PostMapping("/{pseudo}/level-up")
  public PlayerResponse levelUp(@PathVariable String pseudo) {
    return service.levelUp(pseudo);
  }

  @PostMapping("/{pseudo}/monsters")
  public PlayerResponse acquireMonster(@PathVariable String pseudo, @Valid @RequestBody AcquireMonsterRequest req) {
    return service.acquireMonster(pseudo, req.monsterId());
  }

  @DeleteMapping("/{pseudo}/monsters/{monsterId}")
  public PlayerResponse removeMonster(@PathVariable String pseudo, @PathVariable String monsterId) {
    return service.removeMonster(pseudo, monsterId);
  }
}
