package com.imt.api.CentralAPI.dto;

/**
 * Résultat final d'une invocation renvoyé au front.
 */
public record InvocationResultResponse(
    String invocationId,
    String monsterName,
    String elementType,
    String generatedMonsterId,
    String status
) {}
