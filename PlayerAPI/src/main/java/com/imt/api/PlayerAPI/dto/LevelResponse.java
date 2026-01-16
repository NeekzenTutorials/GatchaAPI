package com.imt.api.PlayerAPI.dto;

public record LevelResponse(int level, long experience, long nextLevelXp, boolean canLevelUp) {}
