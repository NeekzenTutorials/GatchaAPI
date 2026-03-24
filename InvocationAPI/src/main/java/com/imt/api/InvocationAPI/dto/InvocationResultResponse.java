package com.imt.api.InvocationAPI.dto;

/**
 * Résultat d'une invocation : le monstre tiré au sort + l'ID généré dans MonsterAPI.
 */
public record InvocationResultResponse(
    String invocationId,
    String monsterName,
    String elementType,
    String generatedMonsterId,
    String status
) {}
