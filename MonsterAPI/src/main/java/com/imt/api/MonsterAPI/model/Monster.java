package com.imt.api.MonsterAPI.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document("monsters")
public class Monster {

  @Id
  private String id;

  @Indexed
  private String ownerPseudo;

  private String name;
  private ElementType elementType;

  private int level;
  private long experience;
  private long nextLevelXp;
  private int skillPoints;

  private int hp;
  private int atk;
  private int def;
  private int vit;

  private List<Skill> skills = new ArrayList<>();

  public Monster() {}

  public Monster(String ownerPseudo, String name, ElementType elementType) {
    this.ownerPseudo = ownerPseudo;
    this.name = name;
    this.elementType = elementType;
    this.level = 1;
    this.experience = 0;
    this.nextLevelXp = 100;
    this.skillPoints = 0;
    applyBaseStats(elementType);
    this.skills = defaultSkills(elementType);
  }

  private void applyBaseStats(ElementType type) {
    switch (type) {
      case FIRE  -> { hp = 80;  atk = 15; def = 5;  vit = 10; }
      case WATER -> { hp = 100; atk = 10; def = 10; vit = 8;  }
      case WIND  -> { hp = 70;  atk = 12; def = 7;  vit = 15; }
    }
  }

  public void applyLevelUpStats() {
    switch (elementType) {
      case FIRE  -> { hp += 8;  atk += 3; def += 1; vit += 2; }
      case WATER -> { hp += 12; atk += 2; def += 2; vit += 1; }
      case WIND  -> { hp += 6;  atk += 2; def += 1; vit += 3; }
    }
  }

  public int getStatValue(RatioStat stat) {
    return switch (stat) {
      case HP  -> hp;
      case ATK -> atk;
      case DEF -> def;
      case VIT -> vit;
    };
  }

  private static List<Skill> defaultSkills(ElementType type) {
    return switch (type) {
      case FIRE -> List.of(
          new Skill("Fireball",    25, RatioStat.ATK, 0.5, 1, 5),
          new Skill("Flame Burst", 40, RatioStat.ATK, 0.8, 3, 3),
          new Skill("Inferno",     60, RatioStat.HP,  0.3, 5, 3)
      );
      case WATER -> List.of(
          new Skill("Water Jet",   20, RatioStat.ATK, 0.4, 1, 5),
          new Skill("Tidal Wave",  35, RatioStat.DEF, 0.6, 3, 3),
          new Skill("Tsunami",     55, RatioStat.HP,  0.4, 5, 3)
      );
      case WIND -> List.of(
          new Skill("Gust",        22, RatioStat.VIT, 0.5, 1, 5),
          new Skill("Tornado",     38, RatioStat.ATK, 0.7, 3, 3),
          new Skill("Hurricane",   50, RatioStat.VIT, 0.6, 5, 3)
      );
    };
  }

  // getters / setters
  public String getId() { return id; }
  public String getOwnerPseudo() { return ownerPseudo; }
  public String getName() { return name; }
  public ElementType getElementType() { return elementType; }
  public int getLevel() { return level; }
  public long getExperience() { return experience; }
  public long getNextLevelXp() { return nextLevelXp; }
  public int getSkillPoints() { return skillPoints; }
  public int getHp() { return hp; }
  public int getAtk() { return atk; }
  public int getDef() { return def; }
  public int getVit() { return vit; }
  public List<Skill> getSkills() { return skills; }

  public void setId(String id) { this.id = id; }
  public void setOwnerPseudo(String ownerPseudo) { this.ownerPseudo = ownerPseudo; }
  public void setName(String name) { this.name = name; }
  public void setElementType(ElementType elementType) { this.elementType = elementType; }
  public void setLevel(int level) { this.level = level; }
  public void setExperience(long experience) { this.experience = experience; }
  public void setNextLevelXp(long nextLevelXp) { this.nextLevelXp = nextLevelXp; }
  public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
  public void setHp(int hp) { this.hp = hp; }
  public void setAtk(int atk) { this.atk = atk; }
  public void setDef(int def) { this.def = def; }
  public void setVit(int vit) { this.vit = vit; }
  public void setSkills(List<Skill> skills) { this.skills = skills; }
}
