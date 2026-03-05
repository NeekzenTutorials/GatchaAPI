package com.imt.api.CentralAPI.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMonsterRequest(
    @NotBlank String name,
    @NotBlank String elementType
) {}
