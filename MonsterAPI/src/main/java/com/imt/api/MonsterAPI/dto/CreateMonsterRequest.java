package com.imt.api.MonsterAPI.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMonsterRequest(
    @NotBlank String name,
    @NotBlank String elementType
) {}
