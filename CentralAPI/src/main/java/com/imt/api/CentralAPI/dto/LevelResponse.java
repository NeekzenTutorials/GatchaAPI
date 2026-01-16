package com.imt.api.CentralAPI.dto;

public record LevelResponse(int level, long experience, long nextLevelXp, boolean canLevelUp) {}
