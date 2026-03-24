package com.imt.api.InvocationAPI.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Buffer d'invocation : enregistre chaque invocation et son état
 * pour permettre la re-création en cas de panne réseau/serveur.
 */
@Document("invocation_records")
public class InvocationRecord {

  @Id
  private String id;

  @Indexed
  private String playerPseudo;

  /** ID du template de monstre tiré au sort */
  private String monsterTemplateId;

  /** Nom du monstre invoqué */
  private String monsterName;

  /** Type élémentaire */
  private String elementType;

  /** ID du monstre créé dans MonsterAPI (null tant que pas créé) */
  private String generatedMonsterId;

  private InvocationStatus status;

  private Instant createdAt;
  private Instant updatedAt;

  public InvocationRecord() {}

  public InvocationRecord(String playerPseudo, String monsterTemplateId,
                          String monsterName, String elementType) {
    this.playerPseudo = playerPseudo;
    this.monsterTemplateId = monsterTemplateId;
    this.monsterName = monsterName;
    this.elementType = elementType;
    this.status = InvocationStatus.ROLLED;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public String getId() { return id; }
  public String getPlayerPseudo() { return playerPseudo; }
  public String getMonsterTemplateId() { return monsterTemplateId; }
  public String getMonsterName() { return monsterName; }
  public String getElementType() { return elementType; }
  public String getGeneratedMonsterId() { return generatedMonsterId; }
  public InvocationStatus getStatus() { return status; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }

  public void setId(String id) { this.id = id; }
  public void setPlayerPseudo(String playerPseudo) { this.playerPseudo = playerPseudo; }
  public void setMonsterTemplateId(String monsterTemplateId) { this.monsterTemplateId = monsterTemplateId; }
  public void setMonsterName(String monsterName) { this.monsterName = monsterName; }
  public void setElementType(String elementType) { this.elementType = elementType; }
  public void setGeneratedMonsterId(String generatedMonsterId) { this.generatedMonsterId = generatedMonsterId; }
  public void setStatus(InvocationStatus status) { this.status = status; this.updatedAt = Instant.now(); }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
