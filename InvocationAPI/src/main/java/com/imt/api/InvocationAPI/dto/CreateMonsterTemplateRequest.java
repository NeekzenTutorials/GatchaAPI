package com.imt.api.InvocationAPI.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateMonsterTemplateRequest(
    @NotBlank String name,
    @NotBlank String elementType,
    int hp,
    int atk,
    int def,
    int vit,
    double invocationRate,
    @NotNull List<SkillTemplateInput> skills
) {}
