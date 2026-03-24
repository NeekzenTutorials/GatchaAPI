package com.imt.api.InvocationAPI.dto;

import jakarta.validation.constraints.NotBlank;

public record SkillTemplateInput(
    @NotBlank String name,
    int baseDamage,
    @NotBlank String ratioStat,
    double damageRatio,
    int cooldown,
    int maxUpgradeLevel
) {}
