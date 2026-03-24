package com.imt.api.InvocationAPI.model;

public enum InvocationStatus {
  /** Monstre tiré au sort, pas encore envoyé à MonsterAPI */
  ROLLED,
  /** Monstre créé dans MonsterAPI, id récupéré */
  MONSTER_CREATED,
  /** Monstre ajouté dans la liste du joueur via PlayerAPI */
  COMPLETED,
  /** Échec lors d'une étape – pourra être re-traité */
  FAILED
}
