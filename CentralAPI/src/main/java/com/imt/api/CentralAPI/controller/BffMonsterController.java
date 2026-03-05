package com.imt.api.CentralAPI.controller;

import com.imt.api.CentralAPI.client.MonsterClient;
import com.imt.api.CentralAPI.client.PlayerClient;
import com.imt.api.CentralAPI.config.OpenApiConfig;
import com.imt.api.CentralAPI.dto.*;
import com.imt.api.CentralAPI.service.AuthGateway;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Monster (BFF)", description = "Gestion des monstres exposée au front via CentralAPI (token -> pseudo)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/monsters")
public class BffMonsterController {

  private final AuthGateway authGateway;
  private final MonsterClient monsterClient;
  private final PlayerClient playerClient;

  public BffMonsterController(AuthGateway authGateway,
                              MonsterClient monsterClient,
                              PlayerClient playerClient) {
    this.authGateway = authGateway;
    this.monsterClient = monsterClient;
    this.playerClient = playerClient;
  }

  /** Créer un monstre et l'ajouter à la liste du joueur */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MonsterDetailResponse create(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @Valid @RequestBody CreateMonsterRequest req
  ) {
    var v = authGateway.validate(authorization);
    // 1. Créer le monstre dans MonsterAPI
    var monster = monsterClient.create(v.pseudo(), req);
    // 2. Ajouter l'ID du monstre à la liste du joueur dans PlayerAPI
    try {
      playerClient.acquireMonster(v.pseudo(), new AcquireMonsterRequest(monster.id()));
    } catch (RuntimeException ex) {
      // En cas d'échec, supprimer le monstre créé pour rester cohérent
      monsterClient.delete(v.pseudo(), monster.id());
      throw ex;
    }
    return monster;
  }

  /** Lister tous les monstres du joueur */
  @GetMapping
  public List<MonsterDetailResponse> list(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
  ) {
    var v = authGateway.validate(authorization);
    return monsterClient.listByOwner(v.pseudo());
  }

  /** Détail d'un monstre du joueur */
  @GetMapping("/{monsterId}")
  public MonsterDetailResponse detail(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @PathVariable String monsterId
  ) {
    var v = authGateway.validate(authorization);
    return monsterClient.detail(v.pseudo(), monsterId);
  }

  /** Gain d'XP pour un monstre */
  @PostMapping("/{monsterId}/xp")
  public MonsterDetailResponse gainXp(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @PathVariable String monsterId,
      @Valid @RequestBody GainXpRequest req
  ) {
    var v = authGateway.validate(authorization);
    return monsterClient.gainXp(v.pseudo(), monsterId, req);
  }

  /** Améliorer une compétence d'un monstre */
  @PostMapping("/{monsterId}/upgrade-skill")
  public MonsterDetailResponse upgradeSkill(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @PathVariable String monsterId,
      @Valid @RequestBody UpgradeSkillRequest req
  ) {
    var v = authGateway.validate(authorization);
    return monsterClient.upgradeSkill(v.pseudo(), monsterId, req);
  }

  /** Supprimer un monstre et le retirer de la liste du joueur */
  @DeleteMapping("/{monsterId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @PathVariable String monsterId
  ) {
    var v = authGateway.validate(authorization);
    // 1. Retirer le monstre de la liste du joueur
    try {
      playerClient.removeMonster(v.pseudo(), monsterId);
    } catch (RuntimeException ignored) {
      // Le monstre n'était peut-être pas dans la liste du joueur
    }
    // 2. Supprimer le monstre dans MonsterAPI
    monsterClient.delete(v.pseudo(), monsterId);
  }
}
