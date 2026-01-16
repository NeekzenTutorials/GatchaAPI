package com.imt.api.CentralAPI.dto;

import jakarta.validation.constraints.NotBlank;

public record AcquireMonsterRequest(@NotBlank String monsterId) {}
