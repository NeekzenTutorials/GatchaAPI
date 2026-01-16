package com.imt.api.PlayerAPI.dto;

import jakarta.validation.constraints.Min;

public record GainXpRequest(@Min(1) long amount) {}
