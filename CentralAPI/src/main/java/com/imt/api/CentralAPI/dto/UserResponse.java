package com.imt.api.CentralAPI.dto;

import java.time.Instant;

public record UserResponse(String id, String email, Instant createdAt) {}
