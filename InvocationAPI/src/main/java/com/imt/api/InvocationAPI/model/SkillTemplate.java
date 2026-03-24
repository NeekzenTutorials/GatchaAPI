package com.imt.api.InvocationAPI.model;

public class SkillTemplate {

  private String name;
  private int baseDamage;
  private RatioStat ratioStat;
  private double damageRatio;
  private int cooldown;
  private int maxUpgradeLevel;

  public SkillTemplate() {}

  public SkillTemplate(String name, int baseDamage, RatioStat ratioStat, double damageRatio,
                       int cooldown, int maxUpgradeLevel) {
    this.name = name;
    this.baseDamage = baseDamage;
    this.ratioStat = ratioStat;
    this.damageRatio = damageRatio;
    this.cooldown = cooldown;
    this.maxUpgradeLevel = maxUpgradeLevel;
  }

  public String getName() { return name; }
  public int getBaseDamage() { return baseDamage; }
  public RatioStat getRatioStat() { return ratioStat; }
  public double getDamageRatio() { return damageRatio; }
  public int getCooldown() { return cooldown; }
  public int getMaxUpgradeLevel() { return maxUpgradeLevel; }

  public void setName(String name) { this.name = name; }
  public void setBaseDamage(int baseDamage) { this.baseDamage = baseDamage; }
  public void setRatioStat(RatioStat ratioStat) { this.ratioStat = ratioStat; }
  public void setDamageRatio(double damageRatio) { this.damageRatio = damageRatio; }
  public void setCooldown(int cooldown) { this.cooldown = cooldown; }
  public void setMaxUpgradeLevel(int maxUpgradeLevel) { this.maxUpgradeLevel = maxUpgradeLevel; }
}
