package com.imt.api.InvocationAPI.dto;

import java.util.List;

public record MonsterTemplateResponse(
    String id,
    String name,
    String elementType,
    int hp,
    int atk,
    int def,
    int vit,
    double invocationRate,
    List<SkillTemplateResponse> skills
) {}
