package com.imt.api.PlayerAPI.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document("players")
public class PlayerProfile {

  @Id
  private String id;

  @Indexed(unique = true)
  private String pseudo;

  private int level;
  private long experience;
  private long nextLevelXp;
  private List<String> monsters = new ArrayList<>();

  public PlayerProfile() {}

  public PlayerProfile(String pseudo) {
    this.pseudo = pseudo;
    this.level = 0;
    this.experience = 0;
    this.nextLevelXp = 50;
  }

  public int getMaxMonsters() {
    return 10 + level;
  }

  // getters / setters
  public String getId() { return id; }
  public String getPseudo() { return pseudo; }
  public int getLevel() { return level; }
  public long getExperience() { return experience; }
  public long getNextLevelXp() { return nextLevelXp; }
  public List<String> getMonsters() { return monsters; }

  public void setId(String id) { this.id = id; }
  public void setPseudo(String pseudo) { this.pseudo = pseudo; }
  public void setLevel(int level) { this.level = level; }
  public void setExperience(long experience) { this.experience = experience; }
  public void setNextLevelXp(long nextLevelXp) { this.nextLevelXp = nextLevelXp; }
  public void setMonsters(List<String> monsters) { this.monsters = monsters; }
}
