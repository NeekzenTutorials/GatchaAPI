package com.imt.api.MonsterAPI.controller;

import com.imt.api.MonsterAPI.dto.*;
import com.imt.api.MonsterAPI.service.MonsterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monsters")
public class MonsterController {

  private final MonsterService service;

  public MonsterController(MonsterService service) {
    this.service = service;
  }

  /** Créer un monstre pour un joueur */
  @PostMapping("/{pseudo}")
  @ResponseStatus(HttpStatus.CREATED)
  public MonsterResponse create(
      @PathVariable String pseudo,
      @Valid @RequestBody CreateMonsterRequest req
  ) {
    return service.create(pseudo, req.name(), req.elementType());
  }

  /** Lister tous les monstres d'un joueur */
  @GetMapping("/{pseudo}")
  public List<MonsterResponse> listByOwner(@PathVariable String pseudo) {
    return service.getMonstersByOwner(pseudo);
  }

  /** Détail d'un monstre */
  @GetMapping("/{pseudo}/{monsterId}")
  public MonsterResponse detail(
      @PathVariable String pseudo,
      @PathVariable String monsterId
  ) {
    return service.getMonster(monsterId, pseudo);
  }

  /** Gain d'XP pour un monstre (level-up automatique) */
  @PostMapping("/{pseudo}/{monsterId}/xp")
  public MonsterResponse gainXp(
      @PathVariable String pseudo,
      @PathVariable String monsterId,
      @Valid @RequestBody GainXpRequest req
  ) {
    return service.gainXp(monsterId, pseudo, req.amount());
  }

  /** Améliorer une compétence (dépense un point de compétence) */
  @PostMapping("/{pseudo}/{monsterId}/upgrade-skill")
  public MonsterResponse upgradeSkill(
      @PathVariable String pseudo,
      @PathVariable String monsterId,
      @Valid @RequestBody UpgradeSkillRequest req
  ) {
    return service.upgradeSkill(monsterId, pseudo, req.skillIndex());
  }

  /** Supprimer un monstre */
  @DeleteMapping("/{pseudo}/{monsterId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable String pseudo,
      @PathVariable String monsterId
  ) {
    service.delete(monsterId, pseudo);
  }
}
