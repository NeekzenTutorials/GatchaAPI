package com.imt.api.AuthAPI.dto;

import java.time.Instant;

public record UserResponse(String id, String email, Instant createdAt) {}
