package com.imt.api.MonsterAPI.dto;

import jakarta.validation.constraints.Min;

public record GainXpRequest(@Min(1) long amount) {}
