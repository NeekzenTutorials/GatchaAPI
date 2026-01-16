package com.imt.api.PlayerAPI.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlayerRequest(@NotBlank String pseudo) {}
