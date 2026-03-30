# GatchaAPI

API for Gatcha Game (School Project) — Architecture micro-services Spring Boot + MongoDB.

---

## Architecture

| Service          | Port  | Rôle                                                      |
|------------------|-------|------------------------------------------------------------|
| **CentralAPI**   | 8080  | Back-for-Front (BFF) – point d'entrée unique pour le front |
| **AuthAPI**      | 8081  | Inscription, login, JWT                                    |
| **PlayerAPI**    | 8082  | Profil joueur, XP, niveau, liste de monstres               |
| **MonsterAPI**   | 8083  | Gestion des monstres (stats, skills, XP, level-up)         |
| **InvocationAPI**| 8084  | Gacha / invocation aléatoire de monstres                   |

Chaque service a sa propre base MongoDB. La communication inter-services passe par CentralAPI (orchestration).

```
┌──────────┐     ┌──────────────┐     ┌────────────┐
│  Front   │────▶│  CentralAPI  │────▶│  AuthAPI   │
└──────────┘     │   (BFF)      │     └────────────┘
                 │              │────▶┌────────────┐
                 │              │     │ PlayerAPI  │
                 │              │────▶└────────────┘
                 │              │     ┌────────────┐
                 │              │────▶│ MonsterAPI │
                 │              │     └────────────┘
                 │              │────▶┌──────────────┐
                 └──────────────┘     │InvocationAPI │
                                      └──────────────┘
```

## Lancement

```bash
docker-compose up -d
```

Cette commande démarre maintenant aussi le frontend Vue via Docker :
- Frontend : http://localhost:5173

Les services seront disponibles :
- **Swagger UI** : http://localhost:8080/docs
- **API docs JSON** : http://localhost:8080/v3/api-docs

### Lancer uniquement le frontend en Docker

Si le backend est déjà lancé, vous pouvez démarrer seulement le front :

```bash
docker-compose up -d gacha-frontend
```

Puis ouvrir : http://localhost:5173

---

## Endpoints CentralAPI

### Auth (`/api/auth`)
| Méthode | Endpoint               | Description                     | Auth |
|---------|------------------------|---------------------------------|------|
| POST    | `/api/auth/register`   | Inscription (crée aussi le profil joueur) | Non  |
| POST    | `/api/auth/login`      | Connexion → retourne un JWT     | Non  |
| GET     | `/api/auth/validate`   | Valide le token JWT             | Oui  |

### Player (`/api/player`)
| Méthode | Endpoint                       | Description                    | Auth |
|---------|--------------------------------|--------------------------------|------|
| GET     | `/api/player/profile`          | Profil complet du joueur       | Oui  |
| GET     | `/api/player/level`            | Niveau et XP du joueur         | Oui  |
| GET     | `/api/player/monsters`         | Liste des monstres du joueur   | Oui  |
| POST    | `/api/player/xp`               | Gagner de l'XP                 | Oui  |
| POST    | `/api/player/level-up`         | Monter de niveau               | Oui  |
| POST    | `/api/player/monsters`         | Ajouter un monstre             | Oui  |
| DELETE  | `/api/player/monsters/{id}`    | Retirer un monstre             | Oui  |

### Monsters (`/api/monsters`)
| Méthode | Endpoint                               | Description                   | Auth |
|---------|----------------------------------------|-------------------------------|------|
| POST    | `/api/monsters`                        | Créer un monstre              | Oui  |
| GET     | `/api/monsters`                        | Lister ses monstres           | Oui  |
| GET     | `/api/monsters/{id}`                   | Détail d'un monstre           | Oui  |
| POST    | `/api/monsters/{id}/xp`               | Donner de l'XP à un monstre   | Oui  |
| POST    | `/api/monsters/{id}/upgrade-skill`     | Améliorer une compétence      | Oui  |
| DELETE  | `/api/monsters/{id}`                   | Supprimer un monstre          | Oui  |

### Invocation / Gacha (`/api/invocations`)
| Méthode | Endpoint                            | Description                              | Auth |
|---------|-------------------------------------|------------------------------------------|------|
| GET     | `/api/invocations/templates`        | Liste des monstres invocables + taux     | Oui  |
| POST    | `/api/invocations/summon`           | Invoquer un monstre (gacha)              | Oui  |
| GET     | `/api/invocations/history`          | Historique des invocations du joueur     | Oui  |
| POST    | `/api/invocations/retry-incomplete` | Re-traiter les invocations échouées      | Oui  |

---

## Tests de l'API

Un jeu de tests end-to-end complet est fourni dans le dossier `tests/`. Il teste le parcours complet d'un joueur en 6 phases.

### Prérequis
- Tous les services doivent être démarrés : `docker-compose up -d`
- Attendre ~10s que tous les services soient prêts

### Exécution

**PowerShell (Windows)** :
```powershell
powershell -ExecutionPolicy Bypass -File tests\test_central_api.ps1
```

**Bash (Linux/Mac/Git Bash)** :
```bash
bash tests/test_central_api.sh
```

### Scénario de test

Le script crée un compte unique à chaque exécution (basé sur un timestamp) et déroule le parcours suivant :

#### Phase 1 — Authentification (6 tests)
| # | Test | Résultat attendu |
|---|------|-----------------|
| 1 | Inscription d'un nouveau joueur | HTTP 201 + id et email retournés |
| 2 | Inscription avec email dupliqué | HTTP 409 (Conflict) |
| 3 | Connexion avec bons identifiants | HTTP 200 + token JWT |
| 4 | Connexion avec mauvais mot de passe | HTTP 401 (Unauthorized) |
| 5 | Validation du token JWT | HTTP 200 + pseudo retourné |
| 6 | Validation avec token invalide | HTTP 500 (erreur) |

#### Phase 2 — Profil Joueur (5 tests)
| # | Test | Résultat attendu |
|---|------|-----------------|
| 7 | Consulter le profil | HTTP 200 + pseudo et level |
| 8 | Consulter le niveau | HTTP 200 + level et nextLevelXp |
| 9 | Gagner 100 XP | HTTP 200 + experience mise à jour |
| 10 | Monter de niveau | HTTP 200 ou 400 (selon XP) |
| 11 | Liste des monstres (vide) | HTTP 200 + maxMonsters |

#### Phase 3 — Gestion des Monstres (9 tests)
| # | Test | Résultat attendu |
|---|------|-----------------|
| 12 | Créer un monstre FEU | HTTP 201 + id, type, stats |
| 13 | Créer un monstre EAU | HTTP 201 |
| 14 | Créer avec type invalide (EARTH) | HTTP 400 |
| 15 | Lister les monstres | HTTP 200 |
| 16 | Détail du monstre FEU | HTTP 200 + nom, skills |
| 17 | Donner 500 XP au monstre | HTTP 200 + level up, skill points |
| 18 | Améliorer compétence 0 | HTTP 200 ou 400 |
| 19 | Supprimer le monstre EAU | HTTP 204 |
| 20 | Vérifier suppression | HTTP 404 |

#### Phase 4 — Invocation / Gacha (6 tests)
| # | Test | Résultat attendu |
|---|------|-----------------|
| 21 | Lister les templates disponibles | HTTP 200 |
| 22 | Invoquer un monstre (summon) | HTTP 201 + nom, type, id, COMPLETED |
| 23 | 2ème invocation | HTTP 201 |
| 24 | 3ème invocation | HTTP 201 |
| 25 | Historique des invocations | HTTP 200 |
| 26 | Re-traiter invocations incomplètes | HTTP 200 |

#### Phase 5 — Vérifications croisées (4 tests)
| # | Test | Résultat attendu |
|---|------|-----------------|
| 27 | Monstre invoqué dans la liste du joueur | Présence vérifiée |
| 28 | Détail du monstre invoqué via API Monstres | HTTP 200 + stats |
| 29 | Liste complète (manuels + invoqués) | HTTP 200 |
| 30 | Profil joueur final | HTTP 200 + pseudo, level, monstres |

#### Phase 6 — Sécurité (2 tests)
| # | Test | Résultat attendu |
|---|------|-----------------|
| 31 | Accès au profil sans token | HTTP 500 |
| 32 | Invocation sans token | HTTP 500 |

### Résultat

À la fin, le script affiche un résumé :
```
==============================================================
  RESUME DES TESTS
==============================================================

  Total  : ~94 assertions
  Reussis : ~56
  Echoues : ~6 (car certains tests doivent échouer)
```
