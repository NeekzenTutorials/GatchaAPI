package com.imt.api.PlayerAPI.service;

import com.imt.api.PlayerAPI.dto.PlayerResponse;
import com.imt.api.PlayerAPI.model.PlayerProfile;
import com.imt.api.PlayerAPI.repository.PlayerProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;

@Service
public class PlayerService {

  private final PlayerProfileRepository repo;

  public PlayerService(PlayerProfileRepository repo) {
    this.repo = repo;
  }

  public PlayerResponse create(String pseudo) {
    if (repo.existsByPseudo(pseudo)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Pseudo already exists");
    }
    var saved = repo.save(new PlayerProfile(pseudo));
    return toResponse(saved);
  }

  public PlayerProfile getByPseudoOrThrow(String pseudo) {
    return repo.findByPseudo(pseudo)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
  }

  public PlayerResponse getProfile(String pseudo) {
    return toResponse(getByPseudoOrThrow(pseudo));
  }

  public PlayerResponse gainXp(String pseudo, long amount) {
    var p = getByPseudoOrThrow(pseudo);

    if (p.getLevel() >= 50) {
      // au max, on peut décider de bloquer l'xp
      return toResponse(p);
    }

    p.setExperience(p.getExperience() + amount);
    repo.save(p);
    return toResponse(p);
  }

  public PlayerResponse levelUp(String pseudo) {
    var p = getByPseudoOrThrow(pseudo);

    if (p.getLevel() >= 50) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already max level");
    }

    if (p.getExperience() < p.getNextLevelXp()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough XP to level up");
    }

    // reset xp + increase level
    p.setLevel(p.getLevel() + 1);
    p.setExperience(0);

    // next threshold: ceil(x * 1.1)
    long next = (long) Math.ceil(p.getNextLevelXp() * 1.1);
    p.setNextLevelXp(next);

    repo.save(p);
    return toResponse(p);
  }

  public PlayerResponse acquireMonster(String pseudo, String monsterId) {
    var p = getByPseudoOrThrow(pseudo);

    if (p.getMonsters().size() >= p.getMaxMonsters()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monster list is full");
    }

    // éviter les doublons
    var set = new LinkedHashSet<>(p.getMonsters());
    boolean added = set.add(monsterId);
    if (!added) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Monster already owned");
    }

    p.setMonsters(set.stream().toList());
    repo.save(p);
    return toResponse(p);
  }

  public PlayerResponse removeMonster(String pseudo, String monsterId) {
    var p = getByPseudoOrThrow(pseudo);

    boolean removed = p.getMonsters().remove(monsterId);
    if (!removed) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Monster not found in player's list");
    }

    repo.save(p);
    return toResponse(p);
  }

  private PlayerResponse toResponse(PlayerProfile p) {
    boolean canLevelUp = p.getExperience() >= p.getNextLevelXp() && p.getLevel() < 50;
    return new PlayerResponse(
        p.getId(),
        p.getPseudo(),
        p.getLevel(),
        p.getExperience(),
        p.getNextLevelXp(),
        canLevelUp,
        p.getMaxMonsters(),
        p.getMonsters()
    );
  }
}
