package com.imt.api.PlayerAPI.dto;

import jakarta.validation.constraints.NotBlank;

public record AcquireMonsterRequest(@NotBlank String monsterId) {}
