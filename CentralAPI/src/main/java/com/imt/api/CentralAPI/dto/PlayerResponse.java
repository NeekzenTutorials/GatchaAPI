package com.imt.api.CentralAPI.dto;

import java.util.List;

public record PlayerResponse(
    String id,
    String pseudo,
    int level,
    long experience,
    long nextLevelXp,
    boolean canLevelUp,
    int maxMonsters,
    List<String> monsters
) {}
