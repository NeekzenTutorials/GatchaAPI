package com.imt.api.InvocationAPI.service;

import com.imt.api.InvocationAPI.dto.*;
import com.imt.api.InvocationAPI.model.*;
import com.imt.api.InvocationAPI.repository.InvocationRecordRepository;
import com.imt.api.InvocationAPI.repository.MonsterTemplateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;

@Service
public class InvocationService {

  private final MonsterTemplateRepository templateRepo;
  private final InvocationRecordRepository recordRepo;
  private final SecureRandom random = new SecureRandom();

  public InvocationService(MonsterTemplateRepository templateRepo,
                           InvocationRecordRepository recordRepo) {
    this.templateRepo = templateRepo;
    this.recordRepo = recordRepo;
  }

  /* ─── Gestion des templates ─── */

  public List<MonsterTemplateResponse> getAllTemplates() {
    return templateRepo.findAll().stream().map(this::toTemplateResponse).toList();
  }

  public MonsterTemplateResponse createTemplate(CreateMonsterTemplateRequest req) {
    ElementType type;
    try {
      type = ElementType.valueOf(req.elementType().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid element type. Must be one of: FIRE, WATER, WIND");
    }

    if (templateRepo.existsByName(req.name())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "A monster template with name '" + req.name() + "' already exists");
    }

    List<SkillTemplate> skills = req.skills().stream()
        .map(s -> {
          RatioStat rs;
          try {
            rs = RatioStat.valueOf(s.ratioStat().toUpperCase());
          } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid ratioStat: " + s.ratioStat());
          }
          return new SkillTemplate(s.name(), s.baseDamage(), rs,
              s.damageRatio(), s.cooldown(), s.maxUpgradeLevel());
        })
        .toList();

    var template = new MonsterTemplate(req.name(), type,
        req.hp(), req.atk(), req.def(), req.vit(),
        req.invocationRate(), skills);

    return toTemplateResponse(templateRepo.save(template));
  }

  public void deleteTemplate(String templateId) {
    if (!templateRepo.existsById(templateId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found");
    }
    templateRepo.deleteById(templateId);
  }

  /* ─── Algorithme d'invocation ─── */

  /**
   * Tire au sort un monstre en respectant les probabilités d'invocation.
   * Algorithme : somme cumulative des taux, tirage aléatoire dans [0, totalRate).
   */
  public MonsterTemplate rollMonster() {
    List<MonsterTemplate> templates = templateRepo.findAll();
    if (templates.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          "No monster templates available for invocation");
    }

    double totalRate = templates.stream().mapToDouble(MonsterTemplate::getInvocationRate).sum();
    double roll = random.nextDouble() * totalRate;

    double cumulative = 0;
    for (MonsterTemplate t : templates) {
      cumulative += t.getInvocationRate();
      if (roll < cumulative) {
        return t;
      }
    }

    // Sécurité : retourner le dernier en cas d'erreur de précision flottante
    return templates.get(templates.size() - 1);
  }

  /**
   * Étape 1 : Tirer un monstre au sort et enregistrer dans le buffer.
   */
  public InvocationRecord performRoll(String playerPseudo) {
    MonsterTemplate rolled = rollMonster();

    var record = new InvocationRecord(
        playerPseudo,
        rolled.getId(),
        rolled.getName(),
        rolled.getElementType().name()
    );

    return recordRepo.save(record);
  }

  /**
   * Met à jour le record après la création réussie du monstre dans MonsterAPI.
   */
  public InvocationRecord markMonsterCreated(String recordId, String generatedMonsterId) {
    var record = recordRepo.findById(recordId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invocation record not found"));

    record.setGeneratedMonsterId(generatedMonsterId);
    record.setStatus(InvocationStatus.MONSTER_CREATED);
    return recordRepo.save(record);
  }

  /**
   * Met à jour le record après l'ajout réussi dans la liste du joueur.
   */
  public InvocationRecord markCompleted(String recordId) {
    var record = recordRepo.findById(recordId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invocation record not found"));

    record.setStatus(InvocationStatus.COMPLETED);
    return recordRepo.save(record);
  }

  /**
   * Marque un record en échec.
   */
  public InvocationRecord markFailed(String recordId) {
    var record = recordRepo.findById(recordId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invocation record not found"));

    record.setStatus(InvocationStatus.FAILED);
    return recordRepo.save(record);
  }

  /* ─── Consultation du buffer ─── */

  public List<InvocationRecordResponse> getRecordsByPlayer(String pseudo) {
    return recordRepo.findByPlayerPseudo(pseudo).stream()
        .map(this::toRecordResponse).toList();
  }

  public List<InvocationRecordResponse> getIncompleteRecords() {
    return recordRepo.findByStatusIn(
        List.of(InvocationStatus.ROLLED, InvocationStatus.MONSTER_CREATED, InvocationStatus.FAILED)
    ).stream().map(this::toRecordResponse).toList();
  }

  public InvocationRecordResponse getRecord(String recordId) {
    var r = recordRepo.findById(recordId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invocation record not found"));
    return toRecordResponse(r);
  }

  /* ─── Mapping ─── */

  private MonsterTemplateResponse toTemplateResponse(MonsterTemplate t) {
    var skills = t.getSkills().stream()
        .map(s -> new SkillTemplateResponse(
            s.getName(), s.getBaseDamage(), s.getRatioStat().name(),
            s.getDamageRatio(), s.getCooldown(), s.getMaxUpgradeLevel()
        )).toList();

    return new MonsterTemplateResponse(
        t.getId(), t.getName(), t.getElementType().name(),
        t.getHp(), t.getAtk(), t.getDef(), t.getVit(),
        t.getInvocationRate(), skills
    );
  }

  public InvocationRecordResponse toRecordResponse(InvocationRecord r) {
    return new InvocationRecordResponse(
        r.getId(), r.getPlayerPseudo(), r.getMonsterTemplateId(),
        r.getMonsterName(), r.getElementType(), r.getGeneratedMonsterId(),
        r.getStatus().name(), r.getCreatedAt(), r.getUpdatedAt()
    );
  }
}
