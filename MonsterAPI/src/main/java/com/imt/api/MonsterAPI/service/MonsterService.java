package com.imt.api.MonsterAPI.service;

import com.imt.api.MonsterAPI.dto.MonsterResponse;
import com.imt.api.MonsterAPI.dto.SkillResponse;
import com.imt.api.MonsterAPI.model.ElementType;
import com.imt.api.MonsterAPI.model.Monster;
import com.imt.api.MonsterAPI.model.Skill;
import com.imt.api.MonsterAPI.repository.MonsterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class MonsterService {

  private static final int MAX_LEVEL = 50;

  private final MonsterRepository repo;

  public MonsterService(MonsterRepository repo) {
    this.repo = repo;
  }

  /* ─── Création ─── */
  public MonsterResponse create(String ownerPseudo, String name, String elementTypeStr) {
    ElementType type;
    try {
      type = ElementType.valueOf(elementTypeStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid element type. Must be one of: FIRE, WATER, WIND");
    }

    var monster = new Monster(ownerPseudo, name, type);
    // Les skills par défaut proviennent du constructeur Monster,
    // mais ils sont dans une List.of() immuable → copier en liste mutable
    monster.setSkills(new ArrayList<>(monster.getSkills()));
    var saved = repo.save(monster);
    return toResponse(saved);
  }

  /* ─── Lecture ─── */
  public List<MonsterResponse> getMonstersByOwner(String ownerPseudo) {
    return repo.findByOwnerPseudo(ownerPseudo).stream()
        .map(this::toResponse)
        .toList();
  }

  public MonsterResponse getMonster(String monsterId, String ownerPseudo) {
    return toResponse(getOrThrow(monsterId, ownerPseudo));
  }

  /* ─── Gain d'XP (auto level-up) ─── */
  public MonsterResponse gainXp(String monsterId, String ownerPseudo, long amount) {
    var m = getOrThrow(monsterId, ownerPseudo);

    if (amount <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "XP amount must be > 0");
    }

    if (m.getLevel() >= MAX_LEVEL) {
      return toResponse(m);
    }

    m.setExperience(m.getExperience() + amount);

    while (m.getLevel() < MAX_LEVEL && m.getExperience() >= m.getNextLevelXp()) {
      m.setExperience(m.getExperience() - m.getNextLevelXp());
      m.setLevel(m.getLevel() + 1);
      m.applyLevelUpStats();
      m.setSkillPoints(m.getSkillPoints() + 1);

      long next = (long) Math.ceil(m.getNextLevelXp() * 1.1);
      m.setNextLevelXp(next);
    }

    repo.save(m);
    return toResponse(m);
  }

  /* ─── Amélioration de compétence ─── */
  public MonsterResponse upgradeSkill(String monsterId, String ownerPseudo, int skillIndex) {
    var m = getOrThrow(monsterId, ownerPseudo);

    if (m.getSkillPoints() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No skill points available");
    }

    if (skillIndex < 0 || skillIndex >= m.getSkills().size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid skill index. Must be 0, 1 or 2");
    }

    Skill skill = m.getSkills().get(skillIndex);

    if (skill.getUpgradeLevel() >= skill.getMaxUpgradeLevel()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Skill '" + skill.getName() + "' is already at max upgrade level");
    }

    // Appliquer l'amélioration
    skill.setUpgradeLevel(skill.getUpgradeLevel() + 1);
    skill.setBaseDamage(skill.getBaseDamage() + 5);
    m.setSkillPoints(m.getSkillPoints() - 1);

    repo.save(m);
    return toResponse(m);
  }

  /* ─── Suppression ─── */
  public void delete(String monsterId, String ownerPseudo) {
    var m = getOrThrow(monsterId, ownerPseudo);
    repo.delete(m);
  }

  /* ─── Helpers ─── */
  private Monster getOrThrow(String monsterId, String ownerPseudo) {
    return repo.findByIdAndOwnerPseudo(monsterId, ownerPseudo)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Monster not found or does not belong to this player"));
  }

  private MonsterResponse toResponse(Monster m) {
    List<SkillResponse> skillResponses = m.getSkills().stream()
        .map(s -> new SkillResponse(
            s.getName(),
            s.getBaseDamage(),
            s.getRatioStat().name(),
            s.getDamageRatio(),
            s.getCooldown(),
            s.getUpgradeLevel(),
            s.getMaxUpgradeLevel()
        ))
        .toList();

    return new MonsterResponse(
        m.getId(),
        m.getOwnerPseudo(),
        m.getName(),
        m.getElementType().name(),
        m.getLevel(),
        m.getExperience(),
        m.getNextLevelXp(),
        m.getSkillPoints(),
        m.getHp(),
        m.getAtk(),
        m.getDef(),
        m.getVit(),
        skillResponses
    );
  }
}
