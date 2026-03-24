package com.imt.api.InvocationAPI.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Modèle de monstre dans la base d'invocation.
 * Contient les stats de base + le taux de probabilité d'invocation.
 * Indépendant de la base MonsterAPI.
 */
@Document("monster_templates")
public class MonsterTemplate {

  @Id
  private String id;

  @Indexed(unique = true)
  private String name;

  private ElementType elementType;

  private int hp;
  private int atk;
  private int def;
  private int vit;

  /** Taux de probabilité d'invocation en pourcentage (ex: 5.0 = 5%) */
  private double invocationRate;

  private List<SkillTemplate> skills;

  public MonsterTemplate() {}

  public MonsterTemplate(String name, ElementType elementType, int hp, int atk, int def, int vit,
                         double invocationRate, List<SkillTemplate> skills) {
    this.name = name;
    this.elementType = elementType;
    this.hp = hp;
    this.atk = atk;
    this.def = def;
    this.vit = vit;
    this.invocationRate = invocationRate;
    this.skills = skills;
  }

  public String getId() { return id; }
  public String getName() { return name; }
  public ElementType getElementType() { return elementType; }
  public int getHp() { return hp; }
  public int getAtk() { return atk; }
  public int getDef() { return def; }
  public int getVit() { return vit; }
  public double getInvocationRate() { return invocationRate; }
  public List<SkillTemplate> getSkills() { return skills; }

  public void setId(String id) { this.id = id; }
  public void setName(String name) { this.name = name; }
  public void setElementType(ElementType elementType) { this.elementType = elementType; }
  public void setHp(int hp) { this.hp = hp; }
  public void setAtk(int atk) { this.atk = atk; }
  public void setDef(int def) { this.def = def; }
  public void setVit(int vit) { this.vit = vit; }
  public void setInvocationRate(double invocationRate) { this.invocationRate = invocationRate; }
  public void setSkills(List<SkillTemplate> skills) { this.skills = skills; }
}
