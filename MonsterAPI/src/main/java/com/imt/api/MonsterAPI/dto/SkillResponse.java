package com.imt.api.MonsterAPI.dto;

public record SkillResponse(
    String name,
    int baseDamage,
    String ratioStat,
    double damageRatio,
    int cooldown,
    int upgradeLevel,
    int maxUpgradeLevel
) {}
