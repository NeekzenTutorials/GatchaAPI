package com.imt.api.InvocationAPI.controller;

import com.imt.api.InvocationAPI.dto.*;
import com.imt.api.InvocationAPI.service.InvocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invocations")
public class InvocationController {

  private final InvocationService service;

  public InvocationController(InvocationService service) {
    this.service = service;
  }

  /* ─── Templates ─── */

  /** Lister tous les templates de monstres */
  @GetMapping("/templates")
  public List<MonsterTemplateResponse> listTemplates() {
    return service.getAllTemplates();
  }

  /** Créer un nouveau template de monstre */
  @PostMapping("/templates")
  @ResponseStatus(HttpStatus.CREATED)
  public MonsterTemplateResponse createTemplate(@Valid @RequestBody CreateMonsterTemplateRequest req) {
    return service.createTemplate(req);
  }

  /** Supprimer un template */
  @DeleteMapping("/templates/{templateId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTemplate(@PathVariable String templateId) {
    service.deleteTemplate(templateId);
  }

  /* ─── Invocation (étape 1 : roll uniquement) ─── */

  /** Tirer un monstre au sort pour un joueur (enregistré dans le buffer) */
  @PostMapping("/roll/{pseudo}")
  @ResponseStatus(HttpStatus.CREATED)
  public InvocationRecordResponse roll(@PathVariable String pseudo) {
    var record = service.performRoll(pseudo);
    return service.toRecordResponse(record);
  }

  /* ─── Mise à jour du buffer (appelé par CentralAPI) ─── */

  /** Signaler que le monstre a été créé dans MonsterAPI */
  @PutMapping("/records/{recordId}/monster-created")
  public InvocationRecordResponse monsterCreated(
      @PathVariable String recordId,
      @RequestParam String generatedMonsterId
  ) {
    var record = service.markMonsterCreated(recordId, generatedMonsterId);
    return service.toRecordResponse(record);
  }

  /** Signaler que l'invocation est terminée (monstre ajouté au joueur) */
  @PutMapping("/records/{recordId}/completed")
  public InvocationRecordResponse completed(@PathVariable String recordId) {
    var record = service.markCompleted(recordId);
    return service.toRecordResponse(record);
  }

  /** Signaler un échec */
  @PutMapping("/records/{recordId}/failed")
  public InvocationRecordResponse failed(@PathVariable String recordId) {
    var record = service.markFailed(recordId);
    return service.toRecordResponse(record);
  }

  /* ─── Consultation du buffer ─── */

  /** Historique des invocations d'un joueur */
  @GetMapping("/records/{pseudo}")
  public List<InvocationRecordResponse> recordsByPlayer(@PathVariable String pseudo) {
    return service.getRecordsByPlayer(pseudo);
  }

  /** Toutes les invocations incomplètes (pour re-création) */
  @GetMapping("/records/incomplete")
  public List<InvocationRecordResponse> incomplete() {
    return service.getIncompleteRecords();
  }

  /** Détail d'un record */
  @GetMapping("/records/detail/{recordId}")
  public InvocationRecordResponse recordDetail(@PathVariable String recordId) {
    return service.getRecord(recordId);
  }
}
