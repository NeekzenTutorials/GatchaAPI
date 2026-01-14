package com.imt.api.AuthAPI.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("users")
public class UserAccount {

  @Id
  private String id;

  @Indexed(unique = true)
  private String email;

  @Indexed(unique = true)
  private String pseudo;

  private String passwordHash;

  private Instant createdAt = Instant.now();

  public UserAccount() {}

  public UserAccount(String email, String pseudo, String passwordHash) {
    this.email = email;
    this.pseudo = pseudo;
    this.passwordHash = passwordHash;
  }

  public String getId() { return id; }
  public String getEmail() { return email; }
  public String getPseudo() { return pseudo; }
  public String getPasswordHash() { return passwordHash; }
  public Instant getCreatedAt() { return createdAt; }

  public void setId(String id) { this.id = id; }
  public void setEmail(String email) { this.email = email; }
  public void setPseudo(String pseudo) { this.pseudo = pseudo; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
