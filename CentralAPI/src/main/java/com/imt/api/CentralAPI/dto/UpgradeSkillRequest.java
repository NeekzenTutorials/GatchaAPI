package com.imt.api.CentralAPI.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpgradeSkillRequest(@Min(0) @Max(2) int skillIndex) {}
