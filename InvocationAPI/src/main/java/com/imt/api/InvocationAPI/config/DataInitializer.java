package com.imt.api.InvocationAPI.config;

import com.imt.api.InvocationAPI.model.ElementType;
import com.imt.api.InvocationAPI.model.MonsterTemplate;
import com.imt.api.InvocationAPI.model.RatioStat;
import com.imt.api.InvocationAPI.model.SkillTemplate;
import com.imt.api.InvocationAPI.repository.MonsterTemplateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initialise la base de templates de monstres au démarrage si elle est vide.
 * Les taux d'invocation totalisent 100%.
 */
@Component
public class DataInitializer implements CommandLineRunner {

  private final MonsterTemplateRepository repo;

  public DataInitializer(MonsterTemplateRepository repo) {
    this.repo = repo;
  }

  @Override
  public void run(String... args) {
    if (repo.count() > 0) {
      return;
    }

    repo.saveAll(List.of(
        // ── FEU (total 35%) ──
        new MonsterTemplate("Ifrit", ElementType.FIRE, 80, 15, 5, 10, 15.0, List.of(
            new SkillTemplate("Fireball",    25, RatioStat.ATK, 0.5, 1, 5),
            new SkillTemplate("Flame Burst", 40, RatioStat.ATK, 0.8, 3, 3),
            new SkillTemplate("Inferno",     60, RatioStat.HP,  0.3, 5, 3)
        )),
        new MonsterTemplate("Phoenix", ElementType.FIRE, 90, 18, 6, 8, 10.0, List.of(
            new SkillTemplate("Blaze",       30, RatioStat.ATK, 0.6, 1, 5),
            new SkillTemplate("Rebirth",     20, RatioStat.HP,  0.9, 4, 3),
            new SkillTemplate("Solar Flare", 70, RatioStat.ATK, 0.4, 5, 3)
        )),
        new MonsterTemplate("Salamander", ElementType.FIRE, 70, 12, 8, 12, 10.0, List.of(
            new SkillTemplate("Ember",       18, RatioStat.ATK, 0.4, 1, 5),
            new SkillTemplate("Heat Wave",   35, RatioStat.VIT, 0.5, 3, 3),
            new SkillTemplate("Magma Pulse", 55, RatioStat.DEF, 0.6, 5, 3)
        )),

        // ── EAU (total 35%) ──
        new MonsterTemplate("Leviathan", ElementType.WATER, 120, 10, 12, 6, 10.0, List.of(
            new SkillTemplate("Water Jet",   20, RatioStat.ATK, 0.4, 1, 5),
            new SkillTemplate("Tidal Wave",  35, RatioStat.DEF, 0.6, 3, 3),
            new SkillTemplate("Tsunami",     55, RatioStat.HP,  0.4, 5, 3)
        )),
        new MonsterTemplate("Kraken", ElementType.WATER, 110, 14, 10, 7, 10.0, List.of(
            new SkillTemplate("Ink Blast",   22, RatioStat.ATK, 0.5, 1, 5),
            new SkillTemplate("Whirlpool",   38, RatioStat.DEF, 0.7, 3, 3),
            new SkillTemplate("Abyss Crush", 58, RatioStat.HP,  0.3, 5, 3)
        )),
        new MonsterTemplate("Undine", ElementType.WATER, 100, 8, 14, 9, 15.0, List.of(
            new SkillTemplate("Aqua Ring",   15, RatioStat.DEF, 0.6, 1, 5),
            new SkillTemplate("Heal Tide",   10, RatioStat.HP,  0.8, 3, 3),
            new SkillTemplate("Deluge",      50, RatioStat.ATK, 0.5, 5, 3)
        )),

        // ── VENT (total 30%) ──
        new MonsterTemplate("Sylph", ElementType.WIND, 70, 12, 7, 15, 15.0, List.of(
            new SkillTemplate("Gust",        22, RatioStat.VIT, 0.5, 1, 5),
            new SkillTemplate("Tornado",     38, RatioStat.ATK, 0.7, 3, 3),
            new SkillTemplate("Hurricane",   50, RatioStat.VIT, 0.6, 5, 3)
        )),
        new MonsterTemplate("Griffin", ElementType.WIND, 85, 14, 8, 13, 10.0, List.of(
            new SkillTemplate("Wing Slash",  28, RatioStat.ATK, 0.6, 1, 5),
            new SkillTemplate("Dive Bomb",   42, RatioStat.VIT, 0.5, 3, 3),
            new SkillTemplate("Tempest",     60, RatioStat.ATK, 0.4, 5, 3)
        )),
        new MonsterTemplate("Thunderbird", ElementType.WIND, 75, 16, 5, 14, 5.0, List.of(
            new SkillTemplate("Spark",       30, RatioStat.ATK, 0.7, 1, 5),
            new SkillTemplate("Thunder Clap",45, RatioStat.VIT, 0.6, 3, 3),
            new SkillTemplate("Storm Fury",  65, RatioStat.ATK, 0.5, 5, 3)
        ))
    ));

    System.out.println("✓ 9 monster templates initialized (rates total 100%)");
  }
}
