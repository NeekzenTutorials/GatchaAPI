package com.imt.api.MonsterAPI.dto;

import java.util.List;

public record MonsterResponse(
    String id,
    String ownerPseudo,
    String name,
    String elementType,
    int level,
    long experience,
    long nextLevelXp,
    int skillPoints,
    int hp,
    int atk,
    int def,
    int vit,
    List<SkillResponse> skills
) {}
