package com.imt.api.CentralAPI.controller;

import com.imt.api.CentralAPI.client.InvocationClient;
import com.imt.api.CentralAPI.client.MonsterClient;
import com.imt.api.CentralAPI.client.PlayerClient;
import com.imt.api.CentralAPI.config.OpenApiConfig;
import com.imt.api.CentralAPI.dto.*;
import com.imt.api.CentralAPI.service.AuthGateway;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Invocation (BFF)", description = "Invocation de monstres – orchestration entre InvocationAPI, MonsterAPI et PlayerAPI")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/invocations")
public class BffInvocationController {

  private final AuthGateway authGateway;
  private final InvocationClient invocationClient;
  private final MonsterClient monsterClient;
  private final PlayerClient playerClient;

  public BffInvocationController(AuthGateway authGateway,
                                 InvocationClient invocationClient,
                                 MonsterClient monsterClient,
                                 PlayerClient playerClient) {
    this.authGateway = authGateway;
    this.invocationClient = invocationClient;
    this.monsterClient = monsterClient;
    this.playerClient = playerClient;
  }

  /**
   * Lister les templates de monstres disponibles pour l'invocation.
   */
  @GetMapping("/templates")
  public List<MonsterTemplateResponse> listTemplates(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
  ) {
    authGateway.validate(authorization);
    return invocationClient.listTemplates();
  }

  /**
   * Invoquer un monstre :
   * 1. InvocationAPI tire au sort un monstre (roll) → record ROLLED
   * 2. MonsterAPI crée le monstre → on récupère l'id → record MONSTER_CREATED
   * 3. PlayerAPI ajoute le monsterId à la liste du joueur → record COMPLETED
   *
   * En cas d'échec à une étape, le record est marqué FAILED.
   */
  @PostMapping("/summon")
  @ResponseStatus(HttpStatus.CREATED)
  public InvocationResultResponse summon(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
  ) {
    var v = authGateway.validate(authorization);
    String pseudo = v.pseudo();

    // Étape 1 : Roll
    InvocationRecordResponse record = invocationClient.roll(pseudo);
    String recordId = record.id();

    // Étape 2 : Créer le monstre dans MonsterAPI
    MonsterDetailResponse monster;
    try {
      var req = new CreateMonsterRequest(record.monsterName(), record.elementType());
      monster = monsterClient.create(pseudo, req);
    } catch (RuntimeException ex) {
      invocationClient.markFailed(recordId);
      throw new RuntimeException("Failed to create monster in MonsterAPI: " + ex.getMessage());
    }

    // Mettre à jour le buffer
    invocationClient.markMonsterCreated(recordId, monster.id());

    // Étape 3 : Ajouter le monstre au joueur
    try {
      playerClient.acquireMonster(pseudo, new AcquireMonsterRequest(monster.id()));
    } catch (RuntimeException ex) {
      invocationClient.markFailed(recordId);
      throw new RuntimeException("Failed to add monster to player: " + ex.getMessage());
    }

    // Marquer comme terminé
    invocationClient.markCompleted(recordId);

    return new InvocationResultResponse(
        recordId,
        record.monsterName(),
        record.elementType(),
        monster.id(),
        "COMPLETED"
    );
  }

  /**
   * Historique des invocations du joueur connecté.
   */
  @GetMapping("/history")
  public List<InvocationRecordResponse> history(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
  ) {
    var v = authGateway.validate(authorization);
    return invocationClient.recordsByPlayer(v.pseudo());
  }

  /**
   * Re-traiter toutes les invocations incomplètes.
   * Reprend chaque record ROLLED, MONSTER_CREATED ou FAILED
   * et tente de compléter les étapes manquantes.
   */
  @PostMapping("/retry-incomplete")
  public List<InvocationResultResponse> retryIncomplete(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
  ) {
    authGateway.validate(authorization);

    List<InvocationRecordResponse> incomplete = invocationClient.incompleteRecords();

    return incomplete.stream().map(record -> {
      String recordId = record.id();
      String pseudo = record.playerPseudo();

      try {
        String monsterId = record.generatedMonsterId();

        // Si le monstre n'a pas encore été créé (ROLLED ou FAILED sans monsterId)
        if (monsterId == null || monsterId.isBlank()) {
          var req = new CreateMonsterRequest(record.monsterName(), record.elementType());
          var monster = monsterClient.create(pseudo, req);
          monsterId = monster.id();
          invocationClient.markMonsterCreated(recordId, monsterId);
        }

        // Si le monstre est créé mais pas encore ajouté au joueur
        if ("ROLLED".equals(record.status()) || "MONSTER_CREATED".equals(record.status())
            || "FAILED".equals(record.status())) {
          playerClient.acquireMonster(pseudo, new AcquireMonsterRequest(monsterId));
          invocationClient.markCompleted(recordId);
        }

        return new InvocationResultResponse(
            recordId, record.monsterName(), record.elementType(), monsterId, "COMPLETED"
        );
      } catch (RuntimeException ex) {
        invocationClient.markFailed(recordId);
        return new InvocationResultResponse(
            recordId, record.monsterName(), record.elementType(),
            record.generatedMonsterId(), "FAILED"
        );
      }
    }).toList();
  }
}
