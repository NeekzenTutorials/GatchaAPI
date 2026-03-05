package com.imt.api.CentralAPI.dto;

import java.util.List;

public record MonsterDetailResponse(
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
    List<SkillDetailResponse> skills
) {}
