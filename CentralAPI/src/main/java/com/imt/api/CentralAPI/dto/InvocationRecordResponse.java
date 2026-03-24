package com.imt.api.CentralAPI.dto;

import java.time.Instant;

public record InvocationRecordResponse(
    String id,
    String playerPseudo,
    String monsterTemplateId,
    String monsterName,
    String elementType,
    String generatedMonsterId,
    String status,
    Instant createdAt,
    Instant updatedAt
) {}
