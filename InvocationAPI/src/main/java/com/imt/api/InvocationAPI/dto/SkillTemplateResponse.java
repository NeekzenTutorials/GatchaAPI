package com.imt.api.InvocationAPI.dto;

public record SkillTemplateResponse(
    String name,
    int baseDamage,
    String ratioStat,
    double damageRatio,
    int cooldown,
    int maxUpgradeLevel
) {}
