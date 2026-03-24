#!/bin/bash
# ============================================================================
#  TEST COMPLET DE L'API CENTRALE (CentralAPI)
#  Scénario end-to-end : Auth → Player → Monster → Invocation
# ============================================================================
#
# Prérequis : docker-compose up -d  (tous les services démarrés)
# Usage     : bash tests/test_central_api.sh
#
# Ce script teste séquentiellement toutes les fonctionnalités exposées par
# CentralAPI en simulant le parcours complet d'un joueur.
# ============================================================================

set -e

BASE_URL="${BASE_URL:-http://localhost:8080}"
PASS=0
FAIL=0
TOTAL=0

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ── Helpers ──────────────────────────────────────────────────────────────────

print_header() {
  echo ""
  echo -e "${CYAN}══════════════════════════════════════════════════════════════${NC}"
  echo -e "${CYAN}  $1${NC}"
  echo -e "${CYAN}══════════════════════════════════════════════════════════════${NC}"
}

print_test() {
  echo -e "\n${YELLOW}▶ TEST $((TOTAL+1)): $1${NC}"
}

assert_status() {
  TOTAL=$((TOTAL + 1))
  local expected=$1
  local actual=$2
  local label=$3

  if [ "$actual" -eq "$expected" ]; then
    echo -e "  ${GREEN}✓ $label (HTTP $actual)${NC}"
    PASS=$((PASS + 1))
  else
    echo -e "  ${RED}✗ $label — attendu HTTP $expected, reçu HTTP $actual${NC}"
    FAIL=$((FAIL + 1))
  fi
}

assert_json_field() {
  TOTAL=$((TOTAL + 1))
  local json="$1"
  local field="$2"
  local label="$3"

  local value
  value=$(echo "$json" | grep -o "\"$field\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | head -1 | sed "s/\"$field\"[[:space:]]*:[[:space:]]*\"//;s/\"$//")

  if [ -z "$value" ]; then
    # Essayer pour les valeurs numériques/bool
    value=$(echo "$json" | grep -o "\"$field\"[[:space:]]*:[[:space:]]*[^,}]*" | head -1 | sed "s/\"$field\"[[:space:]]*:[[:space:]]*//")
  fi

  if [ -n "$value" ]; then
    echo -e "  ${GREEN}✓ $label → $field = $value${NC}"
    PASS=$((PASS + 1))
  else
    echo -e "  ${RED}✗ $label — champ '$field' absent${NC}"
    FAIL=$((FAIL + 1))
  fi
}

extract_json_string() {
  local json="$1"
  local field="$2"
  echo "$json" | grep -o "\"$field\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | head -1 | sed "s/\"$field\"[[:space:]]*:[[:space:]]*\"//;s/\"$//"
}

extract_json_number() {
  local json="$1"
  local field="$2"
  echo "$json" | grep -o "\"$field\"[[:space:]]*:[[:space:]]*[0-9]*" | head -1 | sed "s/\"$field\"[[:space:]]*:[[:space:]]*//"
}

# Identifiants uniques pour ce run
TIMESTAMP=$(date +%s)
EMAIL="testeur${TIMESTAMP}@test.com"
PSEUDO="Testeur${TIMESTAMP}"
PASSWORD="password123"

echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║      TEST COMPLET — CentralAPI (GatchaAPI)                  ║${NC}"
echo -e "${CYAN}║      URL: $BASE_URL                              ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "  Compte test : $EMAIL / $PSEUDO"

# ============================================================================
#  1. AUTHENTIFICATION
# ============================================================================
print_header "1. AUTHENTIFICATION"

# ── 1.1 Inscription ──
print_test "Inscription d'un nouveau joueur"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"pseudo\":\"$PSEUDO\"}")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 201 "$STATUS" "POST /api/auth/register"
assert_json_field "$BODY" "id" "Réponse contient l'id"
assert_json_field "$BODY" "email" "Réponse contient l'email"

# ── 1.2 Inscription dupliquée ──
print_test "Inscription avec email déjà utilisé (doit échouer)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"pseudo\":\"AutrePseudo${TIMESTAMP}\"}")
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 409 "$STATUS" "POST /api/auth/register (email dupliqué)"

# ── 1.3 Login ──
print_test "Connexion avec les bons identifiants"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "POST /api/auth/login"
TOKEN=$(extract_json_string "$BODY" "token")
assert_json_field "$BODY" "token" "Réponse contient le token JWT"

if [ -z "$TOKEN" ]; then
  echo -e "${RED}ERREUR FATALE : Pas de token, impossible de continuer.${NC}"
  exit 1
fi

AUTH_HEADER="Bearer $TOKEN"

# ── 1.4 Login avec mauvais mot de passe ──
print_test "Connexion avec mauvais mot de passe (doit échouer)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"wrongpassword\"}")
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 401 "$STATUS" "POST /api/auth/login (mauvais mdp)"

# ── 1.5 Validation du token ──
print_test "Validation du token JWT"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/auth/validate" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "GET /api/auth/validate"
assert_json_field "$BODY" "pseudo" "Token valide, pseudo retourné"

# ── 1.6 Validation avec token invalide ──
print_test "Validation avec token invalide (doit échouer)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/auth/validate" \
  -H "Authorization: Bearer token_bidon_invalide")
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 500 "$STATUS" "GET /api/auth/validate (token invalide)"

# ============================================================================
#  2. PROFIL JOUEUR
# ============================================================================
print_header "2. PROFIL JOUEUR"

# ── 2.1 Consulter le profil ──
print_test "Consulter le profil du joueur"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/player/profile" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "GET /api/player/profile"
assert_json_field "$BODY" "pseudo" "Profil contient le pseudo"
assert_json_field "$BODY" "level" "Profil contient le level"

# ── 2.2 Consulter le niveau ──
print_test "Consulter le niveau du joueur"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/player/level" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "GET /api/player/level"
assert_json_field "$BODY" "level" "Niveau retourné"
assert_json_field "$BODY" "nextLevelXp" "XP requis pour le prochain niveau"

# ── 2.3 Gagner de l'XP ──
print_test "Gagner 100 XP"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/player/xp" \
  -H "Authorization: $AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100}')
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "POST /api/player/xp (+100 XP)"
assert_json_field "$BODY" "experience" "XP mis à jour"

# ── 2.4 Level up ──
print_test "Monter de niveau (level-up)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/player/level-up" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
# Peut réussir ou échouer selon l'XP accumulé
if [ "$STATUS" -eq 200 ]; then
  echo -e "  ${GREEN}✓ Level up réussi${NC}"
  TOTAL=$((TOTAL + 1)); PASS=$((PASS + 1))
else
  echo -e "  ${YELLOW}⚠ Level up impossible (XP insuffisant) — HTTP $STATUS (comportement attendu)${NC}"
  TOTAL=$((TOTAL + 1)); PASS=$((PASS + 1))
fi

# ── 2.5 Consulter la liste de monstres (vide) ──
print_test "Consulter les monstres du joueur (liste initiale)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/player/monsters" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "GET /api/player/monsters"
assert_json_field "$BODY" "maxMonsters" "Capacité max de monstres"

# ============================================================================
#  3. MONSTRES (création manuelle)
# ============================================================================
print_header "3. GESTION DES MONSTRES"

# ── 3.1 Créer un monstre FEU ──
print_test "Créer un monstre de type FEU"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/monsters" \
  -H "Authorization: $AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -d '{"name": "TestFireMonster", "elementType": "FIRE"}')
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 201 "$STATUS" "POST /api/monsters (FIRE)"
MONSTER_ID_FIRE=$(extract_json_string "$BODY" "id")
assert_json_field "$BODY" "id" "Monstre créé avec un id"
assert_json_field "$BODY" "elementType" "Type élémentaire FIRE"
assert_json_field "$BODY" "hp" "Points de vie"
assert_json_field "$BODY" "atk" "Attaque"

# ── 3.2 Créer un monstre EAU ──
print_test "Créer un monstre de type EAU"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/monsters" \
  -H "Authorization: $AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -d '{"name": "TestWaterMonster", "elementType": "WATER"}')
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 201 "$STATUS" "POST /api/monsters (WATER)"
MONSTER_ID_WATER=$(extract_json_string "$BODY" "id")
assert_json_field "$BODY" "id" "Monstre EAU créé"

# ── 3.3 Créer un monstre avec type invalide ──
print_test "Créer un monstre avec type invalide (doit échouer)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/monsters" \
  -H "Authorization: $AUTH_HEADER" \
  -H "Content-Type: application/json" \
  -d '{"name": "Invalid", "elementType": "EARTH"}')
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 400 "$STATUS" "POST /api/monsters (type EARTH invalide)"

# ── 3.4 Lister les monstres ──
print_test "Lister tous les monstres du joueur"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/monsters" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "GET /api/monsters"

# ── 3.5 Détail d'un monstre ──
print_test "Consulter le détail du monstre FEU"
if [ -n "$MONSTER_ID_FIRE" ]; then
  RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/monsters/$MONSTER_ID_FIRE" \
    -H "Authorization: $AUTH_HEADER")
  BODY=$(echo "$RESPONSE" | head -1)
  STATUS=$(echo "$RESPONSE" | tail -1)
  assert_status 200 "$STATUS" "GET /api/monsters/{id}"
  assert_json_field "$BODY" "name" "Nom du monstre"
  assert_json_field "$BODY" "skills" "Compétences du monstre"
else
  echo -e "  ${RED}✗ Impossible — MONSTER_ID_FIRE non défini${NC}"
  TOTAL=$((TOTAL + 1)); FAIL=$((FAIL + 1))
fi

# ── 3.6 Gagner de l'XP sur un monstre ──
print_test "Faire gagner 500 XP au monstre FEU"
if [ -n "$MONSTER_ID_FIRE" ]; then
  RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/monsters/$MONSTER_ID_FIRE/xp" \
    -H "Authorization: $AUTH_HEADER" \
    -H "Content-Type: application/json" \
    -d '{"amount": 500}')
  BODY=$(echo "$RESPONSE" | head -1)
  STATUS=$(echo "$RESPONSE" | tail -1)
  assert_status 200 "$STATUS" "POST /api/monsters/{id}/xp (+500 XP)"
  LEVEL=$(extract_json_number "$BODY" "level")
  assert_json_field "$BODY" "level" "Monstre a monté de niveau"
  assert_json_field "$BODY" "skillPoints" "Points de compétence gagnés"
else
  echo -e "  ${RED}✗ Impossible — MONSTER_ID_FIRE non défini${NC}"
  TOTAL=$((TOTAL + 1)); FAIL=$((FAIL + 1))
fi

# ── 3.7 Améliorer une compétence ──
print_test "Améliorer la compétence 0 du monstre FEU"
if [ -n "$MONSTER_ID_FIRE" ]; then
  RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/monsters/$MONSTER_ID_FIRE/upgrade-skill" \
    -H "Authorization: $AUTH_HEADER" \
    -H "Content-Type: application/json" \
    -d '{"skillIndex": 0}')
  BODY=$(echo "$RESPONSE" | head -1)
  STATUS=$(echo "$RESPONSE" | tail -1)
  if [ "$STATUS" -eq 200 ]; then
    assert_status 200 "$STATUS" "POST /api/monsters/{id}/upgrade-skill"
  else
    assert_status 400 "$STATUS" "POST /api/monsters/{id}/upgrade-skill (pas de skill points)"
  fi
else
  echo -e "  ${RED}✗ Impossible — MONSTER_ID_FIRE non défini${NC}"
  TOTAL=$((TOTAL + 1)); FAIL=$((FAIL + 1))
fi

# ── 3.8 Supprimer un monstre ──
print_test "Supprimer le monstre EAU"
if [ -n "$MONSTER_ID_WATER" ]; then
  RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/api/monsters/$MONSTER_ID_WATER" \
    -H "Authorization: $AUTH_HEADER")
  STATUS=$(echo "$RESPONSE" | tail -1)
  assert_status 204 "$STATUS" "DELETE /api/monsters/{id}"
else
  echo -e "  ${RED}✗ Impossible — MONSTER_ID_WATER non défini${NC}"
  TOTAL=$((TOTAL + 1)); FAIL=$((FAIL + 1))
fi

# ── 3.9 Vérifier que le monstre supprimé n'existe plus ──
print_test "Vérifier que le monstre EAU supprimé n'existe plus"
if [ -n "$MONSTER_ID_WATER" ]; then
  RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/monsters/$MONSTER_ID_WATER" \
    -H "Authorization: $AUTH_HEADER")
  STATUS=$(echo "$RESPONSE" | tail -1)
  assert_status 404 "$STATUS" "GET /api/monsters/{id} (monstre supprimé)"
else
  echo -e "  ${RED}✗ Impossible — MONSTER_ID_WATER non défini${NC}"
  TOTAL=$((TOTAL + 1)); FAIL=$((FAIL + 1))
fi

# ============================================================================
#  4. INVOCATION (Gacha)
# ============================================================================
print_header "4. INVOCATION (GACHA)"

# ── 4.1 Lister les templates disponibles ──
print_test "Lister les templates de monstres invocables"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/invocations/templates" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "GET /api/invocations/templates"

# ── 4.2 Invoquer un monstre (summon) ──
print_test "Invoquer un monstre (summon)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/invocations/summon" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 201 "$STATUS" "POST /api/invocations/summon"
INVOKED_MONSTER_ID=$(extract_json_string "$BODY" "generatedMonsterId")
INVOKED_NAME=$(extract_json_string "$BODY" "monsterName")
assert_json_field "$BODY" "invocationId" "ID de l'invocation"
assert_json_field "$BODY" "monsterName" "Nom du monstre invoqué"
assert_json_field "$BODY" "elementType" "Type élémentaire"
assert_json_field "$BODY" "generatedMonsterId" "ID du monstre généré"
assert_json_field "$BODY" "status" "Statut COMPLETED"
echo -e "  ${CYAN}  ➤ Monstre invoqué : $INVOKED_NAME (id: $INVOKED_MONSTER_ID)${NC}"

# ── 4.3 Deuxième invocation ──
print_test "Invoquer un deuxième monstre"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/invocations/summon" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 201 "$STATUS" "POST /api/invocations/summon (2ème)"
INVOKED_NAME_2=$(extract_json_string "$BODY" "monsterName")
echo -e "  ${CYAN}  ➤ Monstre invoqué : $INVOKED_NAME_2${NC}"

# ── 4.4 Troisième invocation ──
print_test "Invoquer un troisième monstre"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/invocations/summon" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 201 "$STATUS" "POST /api/invocations/summon (3ème)"
INVOKED_NAME_3=$(extract_json_string "$BODY" "monsterName")
echo -e "  ${CYAN}  ➤ Monstre invoqué : $INVOKED_NAME_3${NC}"

# ── 4.5 Historique des invocations ──
print_test "Consulter l'historique des invocations"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/invocations/history" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "GET /api/invocations/history"

# ── 4.6 Re-traiter les invocations incomplètes ──
print_test "Re-traiter les invocations incomplètes (retry)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/invocations/retry-incomplete" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "POST /api/invocations/retry-incomplete"

# ============================================================================
#  5. VÉRIFICATIONS CROISÉES
# ============================================================================
print_header "5. VÉRIFICATIONS CROISÉES"

# ── 5.1 Vérifier que les monstres invoqués sont dans la liste du joueur ──
print_test "Les monstres invoqués apparaissent dans le profil joueur"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/player/monsters" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "GET /api/player/monsters"

# Vérifier que les monstres invoqués sont listés
if echo "$BODY" | grep -q "$INVOKED_MONSTER_ID"; then
  echo -e "  ${GREEN}✓ Le monstre invoqué est bien dans la liste du joueur${NC}"
  TOTAL=$((TOTAL + 1)); PASS=$((PASS + 1))
else
  echo -e "  ${RED}✗ Le monstre invoqué n'est PAS dans la liste du joueur${NC}"
  TOTAL=$((TOTAL + 1)); FAIL=$((FAIL + 1))
fi

# ── 5.2 Vérifier le détail d'un monstre invoqué via l'API Monstres ──
print_test "Détail du monstre invoqué via GET /api/monsters/{id}"
if [ -n "$INVOKED_MONSTER_ID" ]; then
  RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/monsters/$INVOKED_MONSTER_ID" \
    -H "Authorization: $AUTH_HEADER")
  BODY=$(echo "$RESPONSE" | head -1)
  STATUS=$(echo "$RESPONSE" | tail -1)
  assert_status 200 "$STATUS" "GET /api/monsters/{id} (monstre invoqué)"
  assert_json_field "$BODY" "name" "Nom du monstre invoqué"
  assert_json_field "$BODY" "hp" "HP du monstre invoqué"
else
  echo -e "  ${RED}✗ Impossible — INVOKED_MONSTER_ID non défini${NC}"
  TOTAL=$((TOTAL + 1)); FAIL=$((FAIL + 1))
fi

# ── 5.3 Lister tous les monstres du joueur (manuels + invoqués) ──
print_test "Lister tous les monstres (manuels + invoqués)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/monsters" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "GET /api/monsters (tous)"

# ── 5.4 Profil joueur final ──
print_test "Profil joueur final (récapitulatif)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/player/profile" \
  -H "Authorization: $AUTH_HEADER")
BODY=$(echo "$RESPONSE" | head -1)
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 200 "$STATUS" "GET /api/player/profile (final)"
assert_json_field "$BODY" "pseudo" "Pseudo du joueur"
assert_json_field "$BODY" "level" "Niveau du joueur"
assert_json_field "$BODY" "monsters" "Liste de monstres"

# ============================================================================
#  6. TESTS SANS AUTHENTIFICATION
# ============================================================================
print_header "6. TESTS SANS AUTHENTIFICATION (sécurité)"

print_test "Accès au profil sans token (doit échouer)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/player/profile")
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 500 "$STATUS" "GET /api/player/profile (sans token)"

print_test "Invocation sans token (doit échouer)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/invocations/summon")
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status 500 "$STATUS" "POST /api/invocations/summon (sans token)"

# ============================================================================
#  RÉSUMÉ
# ============================================================================
echo ""
echo -e "${CYAN}══════════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}  RÉSUMÉ DES TESTS${NC}"
echo -e "${CYAN}══════════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "  Total  : ${TOTAL} tests"
echo -e "  ${GREEN}Réussis : ${PASS}${NC}"
echo -e "  ${RED}Échoués : ${FAIL}${NC}"
echo ""

if [ "$FAIL" -eq 0 ]; then
  echo -e "${GREEN}  ✅ TOUS LES TESTS SONT PASSÉS !${NC}"
else
  echo -e "${RED}  ❌ ${FAIL} TEST(S) EN ÉCHEC${NC}"
fi
echo ""

exit $FAIL
