package com.imt.api.InvocationAPI;

import com.imt.api.InvocationAPI.model.*;
import com.imt.api.InvocationAPI.repository.InvocationRecordRepository;
import com.imt.api.InvocationAPI.repository.MonsterTemplateRepository;
import com.imt.api.InvocationAPI.service.InvocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitaire de l'algorithme d'invocation.
 * Valide que les taux de probabilité sont bien respectés sur un grand nombre de tirages.
 */
@ExtendWith(MockitoExtension.class)
class InvocationAlgorithmTest {

  @Mock
  private MonsterTemplateRepository templateRepo;

  @Mock
  private InvocationRecordRepository recordRepo;

  private InvocationService service;

  private List<MonsterTemplate> templates;

  @BeforeEach
  void setUp() {
    service = new InvocationService(templateRepo, recordRepo);

    // 3 monstres avec des taux différents : 50%, 30%, 20%
    var common = new MonsterTemplate("Common", ElementType.FIRE, 80, 15, 5, 10, 50.0, List.of(
        new SkillTemplate("Fireball", 25, RatioStat.ATK, 0.5, 1, 5),
        new SkillTemplate("Flame Burst", 40, RatioStat.ATK, 0.8, 3, 3),
        new SkillTemplate("Inferno", 60, RatioStat.HP, 0.3, 5, 3)
    ));
    common.setId("1");

    var uncommon = new MonsterTemplate("Uncommon", ElementType.WATER, 100, 10, 10, 8, 30.0, List.of(
        new SkillTemplate("Water Jet", 20, RatioStat.ATK, 0.4, 1, 5),
        new SkillTemplate("Tidal Wave", 35, RatioStat.DEF, 0.6, 3, 3),
        new SkillTemplate("Tsunami", 55, RatioStat.HP, 0.4, 5, 3)
    ));
    uncommon.setId("2");

    var rare = new MonsterTemplate("Rare", ElementType.WIND, 70, 12, 7, 15, 20.0, List.of(
        new SkillTemplate("Gust", 22, RatioStat.VIT, 0.5, 1, 5),
        new SkillTemplate("Tornado", 38, RatioStat.ATK, 0.7, 3, 3),
        new SkillTemplate("Hurricane", 50, RatioStat.VIT, 0.6, 5, 3)
    ));
    rare.setId("3");

    templates = List.of(common, uncommon, rare);
  }

  @Test
  void rollMonster_respectsProbabilities() {
    when(templateRepo.findAll()).thenReturn(templates);

    int iterations = 100_000;
    Map<String, Integer> counts = new HashMap<>();

    for (int i = 0; i < iterations; i++) {
      MonsterTemplate rolled = service.rollMonster();
      counts.merge(rolled.getName(), 1, Integer::sum);
    }

    double commonPct = counts.getOrDefault("Common", 0) * 100.0 / iterations;
    double uncommonPct = counts.getOrDefault("Uncommon", 0) * 100.0 / iterations;
    double rarePct = counts.getOrDefault("Rare", 0) * 100.0 / iterations;

    System.out.printf("Common:   %.2f%% (expected 50%%)%n", commonPct);
    System.out.printf("Uncommon: %.2f%% (expected 30%%)%n", uncommonPct);
    System.out.printf("Rare:     %.2f%% (expected 20%%)%n", rarePct);

    // Tolérance de 2% d'écart
    double tolerance = 2.0;
    assertEquals(50.0, commonPct, tolerance, "Common rate should be ~50%");
    assertEquals(30.0, uncommonPct, tolerance, "Uncommon rate should be ~30%");
    assertEquals(20.0, rarePct, tolerance, "Rare rate should be ~20%");
  }

  @Test
  void rollMonster_allMonstersCanBeRolled() {
    when(templateRepo.findAll()).thenReturn(templates);

    Map<String, Integer> counts = new HashMap<>();
    for (int i = 0; i < 10_000; i++) {
      MonsterTemplate rolled = service.rollMonster();
      counts.merge(rolled.getName(), 1, Integer::sum);
    }

    // Chaque monstre doit apparaître au moins une fois
    assertTrue(counts.containsKey("Common"), "Common should appear");
    assertTrue(counts.containsKey("Uncommon"), "Uncommon should appear");
    assertTrue(counts.containsKey("Rare"), "Rare should appear");
  }

  @Test
  void rollMonster_singleTemplate_alwaysReturnsSame() {
    var single = new MonsterTemplate("Only", ElementType.FIRE, 80, 15, 5, 10, 100.0, List.of(
        new SkillTemplate("Fireball", 25, RatioStat.ATK, 0.5, 1, 5),
        new SkillTemplate("Flame Burst", 40, RatioStat.ATK, 0.8, 3, 3),
        new SkillTemplate("Inferno", 60, RatioStat.HP, 0.3, 5, 3)
    ));
    single.setId("solo");

    when(templateRepo.findAll()).thenReturn(List.of(single));

    for (int i = 0; i < 1_000; i++) {
      assertEquals("Only", service.rollMonster().getName());
    }
  }

  @Test
  void rollMonster_withNineTemplates_respectsProbabilities() {
    // Simuler les 9 monstres du DataInitializer
    List<MonsterTemplate> nineTemplates = List.of(
        makeTemplate("1", "Ifrit",       ElementType.FIRE,  15.0),
        makeTemplate("2", "Phoenix",     ElementType.FIRE,  10.0),
        makeTemplate("3", "Salamander",  ElementType.FIRE,  10.0),
        makeTemplate("4", "Leviathan",   ElementType.WATER, 10.0),
        makeTemplate("5", "Kraken",      ElementType.WATER, 10.0),
        makeTemplate("6", "Undine",      ElementType.WATER, 15.0),
        makeTemplate("7", "Sylph",       ElementType.WIND,  15.0),
        makeTemplate("8", "Griffin",     ElementType.WIND,  10.0),
        makeTemplate("9", "Thunderbird", ElementType.WIND,   5.0)
    );

    when(templateRepo.findAll()).thenReturn(nineTemplates);

    int iterations = 200_000;
    Map<String, Integer> counts = new HashMap<>();

    for (int i = 0; i < iterations; i++) {
      MonsterTemplate rolled = service.rollMonster();
      counts.merge(rolled.getName(), 1, Integer::sum);
    }

    double tolerance = 1.5;

    // Vérifier chaque monstre
    assertRate(counts, "Ifrit",       15.0, iterations, tolerance);
    assertRate(counts, "Phoenix",     10.0, iterations, tolerance);
    assertRate(counts, "Salamander",  10.0, iterations, tolerance);
    assertRate(counts, "Leviathan",   10.0, iterations, tolerance);
    assertRate(counts, "Kraken",      10.0, iterations, tolerance);
    assertRate(counts, "Undine",      15.0, iterations, tolerance);
    assertRate(counts, "Sylph",       15.0, iterations, tolerance);
    assertRate(counts, "Griffin",     10.0, iterations, tolerance);
    assertRate(counts, "Thunderbird",  5.0, iterations, tolerance);
  }

  private void assertRate(Map<String, Integer> counts, String name, double expectedPct,
                          int total, double tolerance) {
    double actual = counts.getOrDefault(name, 0) * 100.0 / total;
    System.out.printf("%-12s: %.2f%% (expected %.1f%%)%n", name, actual, expectedPct);
    assertEquals(expectedPct, actual, tolerance,
        name + " rate should be ~" + expectedPct + "% but was " + actual + "%");
  }

  private MonsterTemplate makeTemplate(String id, String name, ElementType type, double rate) {
    var t = new MonsterTemplate(name, type, 80, 15, 5, 10, rate, List.of(
        new SkillTemplate("Skill1", 25, RatioStat.ATK, 0.5, 1, 5),
        new SkillTemplate("Skill2", 40, RatioStat.ATK, 0.8, 3, 3),
        new SkillTemplate("Skill3", 60, RatioStat.HP,  0.3, 5, 3)
    ));
    t.setId(id);
    return t;
  }
}
