package com.imt.api.CentralAPI.client;

import com.imt.api.CentralAPI.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class MonsterClient {

  private final WebClient web;

  public MonsterClient(@Value("${monster.base-url}") String baseUrl, WebClient.Builder builder) {
    this.web = builder.baseUrl(baseUrl).build();
  }

  /** Créer un monstre pour un joueur */
  public MonsterDetailResponse create(String pseudo, CreateMonsterRequest req) {
    return web.post().uri("/monsters/{pseudo}", pseudo)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(MonsterDetailResponse.class)
        .block();
  }

  /** Lister tous les monstres d'un joueur */
  public List<MonsterDetailResponse> listByOwner(String pseudo) {
    return web.get().uri("/monsters/{pseudo}", pseudo)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<MonsterDetailResponse>>() {})
        .block();
  }

  /** Détail d'un monstre */
  public MonsterDetailResponse detail(String pseudo, String monsterId) {
    return web.get().uri("/monsters/{pseudo}/{monsterId}", pseudo, monsterId)
        .retrieve()
        .bodyToMono(MonsterDetailResponse.class)
        .block();
  }

  /** Gain d'XP pour un monstre */
  public MonsterDetailResponse gainXp(String pseudo, String monsterId, GainXpRequest req) {
    return web.post().uri("/monsters/{pseudo}/{monsterId}/xp", pseudo, monsterId)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(MonsterDetailResponse.class)
        .block();
  }

  /** Améliorer une compétence */
  public MonsterDetailResponse upgradeSkill(String pseudo, String monsterId, UpgradeSkillRequest req) {
    return web.post().uri("/monsters/{pseudo}/{monsterId}/upgrade-skill", pseudo, monsterId)
        .bodyValue(req)
        .retrieve()
        .bodyToMono(MonsterDetailResponse.class)
        .block();
  }

  /** Supprimer un monstre */
  public void delete(String pseudo, String monsterId) {
    web.delete().uri("/monsters/{pseudo}/{monsterId}", pseudo, monsterId)
        .retrieve()
        .toBodilessEntity()
        .block();
  }
}
