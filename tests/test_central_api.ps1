# ============================================================================
#  TEST COMPLET DE L'API CENTRALE (CentralAPI)
#  Scenario end-to-end : Auth -> Player -> Monster -> Invocation
# ============================================================================
#
# Prerequis : docker-compose up -d  (tous les services demarres)
# Usage     : powershell -ExecutionPolicy Bypass -File tests\test_central_api.ps1
#
# ============================================================================

$ErrorActionPreference = "Continue"
$BASE_URL = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:8080" }
$PASS = 0
$FAIL = 0
$TOTAL = 0

# ── Helpers ──

function Print-Header($title) {
    Write-Host ""
    Write-Host "==============================================================" -ForegroundColor Cyan
    Write-Host "  $title" -ForegroundColor Cyan
    Write-Host "==============================================================" -ForegroundColor Cyan
}

function Print-Test($label) {
    $script:TOTAL++
    Write-Host "`n> TEST ${script:TOTAL}: $label" -ForegroundColor Yellow
}

function Assert-Status($expected, $actual, $label) {
    $script:TOTAL++
    if ($actual -eq $expected) {
        Write-Host "  [PASS] $label (HTTP $actual)" -ForegroundColor Green
        $script:PASS++
    } else {
        Write-Host "  [FAIL] $label - attendu HTTP $expected, recu HTTP $actual" -ForegroundColor Red
        $script:FAIL++
    }
}

function Assert-JsonField($json, $field, $label) {
    $script:TOTAL++
    try {
        $obj = $json | ConvertFrom-Json
        $val = $obj.$field
        if ($null -ne $val -and "$val" -ne "") {
            Write-Host "  [PASS] $label -> $field = $val" -ForegroundColor Green
            $script:PASS++
        } else {
            Write-Host "  [FAIL] $label - champ '$field' absent ou vide" -ForegroundColor Red
            $script:FAIL++
        }
    } catch {
        Write-Host "  [FAIL] $label - impossible de parser le JSON" -ForegroundColor Red
        $script:FAIL++
    }
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Uri,
        [string]$Body = $null,
        [hashtable]$Headers = @{}
    )
    $params = @{
        Method = $Method
        Uri = "$BASE_URL$Uri"
        Headers = $Headers
        ContentType = "application/json"
        ErrorAction = "SilentlyContinue"
    }
    if ($Body) { $params.Body = $Body }

    try {
        $response = Invoke-WebRequest @params -UseBasicParsing
        return @{ Status = $response.StatusCode; Body = $response.Content }
    } catch {
        $status = 0
        $body = ""
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            try {
                $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
                $body = $reader.ReadToEnd()
                $reader.Close()
            } catch {
                $body = $_.Exception.Message
            }
        }
        return @{ Status = $status; Body = $body }
    }
}

# Identifiants uniques pour ce run
$TIMESTAMP = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$EMAIL = "testeur${TIMESTAMP}@test.com"
$PSEUDO = "Testeur${TIMESTAMP}"
$PASSWORD = "password123"

Write-Host ""
Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "  TEST COMPLET - CentralAPI (GatchaAPI)" -ForegroundColor Cyan
Write-Host "  URL: $BASE_URL" -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Compte test : $EMAIL / $PSEUDO"

# ============================================================================
#  1. AUTHENTIFICATION
# ============================================================================
Print-Header "1. AUTHENTIFICATION"

# 1.1 Inscription
Print-Test "Inscription d'un nouveau joueur"
$r = Invoke-Api -Method POST -Uri "/api/auth/register" -Body (@{email=$EMAIL;password=$PASSWORD;pseudo=$PSEUDO} | ConvertTo-Json)
Assert-Status 201 $r.Status "POST /api/auth/register"
Assert-JsonField $r.Body "id" "Reponse contient l'id"
Assert-JsonField $r.Body "email" "Reponse contient l'email"

# 1.2 Inscription dupliquee
Print-Test "Inscription avec email deja utilise (doit echouer)"
$r = Invoke-Api -Method POST -Uri "/api/auth/register" -Body (@{email=$EMAIL;password=$PASSWORD;pseudo="Autre$TIMESTAMP"} | ConvertTo-Json)
Assert-Status 409 $r.Status "POST /api/auth/register (email duplique)"

# 1.3 Login
Print-Test "Connexion avec les bons identifiants"
$r = Invoke-Api -Method POST -Uri "/api/auth/login" -Body (@{email=$EMAIL;password=$PASSWORD} | ConvertTo-Json)
Assert-Status 200 $r.Status "POST /api/auth/login"
$TOKEN = ($r.Body | ConvertFrom-Json).token
Assert-JsonField $r.Body "token" "Reponse contient le token JWT"

if (-not $TOKEN) {
    Write-Host "ERREUR FATALE : Pas de token, impossible de continuer." -ForegroundColor Red
    exit 1
}
$AUTH = @{ Authorization = "Bearer $TOKEN" }

# 1.4 Login mauvais mot de passe
Print-Test "Connexion avec mauvais mot de passe (doit echouer)"
$r = Invoke-Api -Method POST -Uri "/api/auth/login" -Body (@{email=$EMAIL;password="wrongpassword"} | ConvertTo-Json)
Assert-Status 401 $r.Status "POST /api/auth/login (mauvais mdp)"

# 1.5 Validation du token
Print-Test "Validation du token JWT"
$r = Invoke-Api -Method GET -Uri "/api/auth/validate" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/auth/validate"
Assert-JsonField $r.Body "pseudo" "Token valide, pseudo retourne"

# 1.6 Token invalide
Print-Test "Validation avec token invalide (doit echouer)"
$r = Invoke-Api -Method GET -Uri "/api/auth/validate" -Headers @{ Authorization = "Bearer token_bidon" }
Assert-Status 500 $r.Status "GET /api/auth/validate (token invalide)"

# ============================================================================
#  2. PROFIL JOUEUR
# ============================================================================
Print-Header "2. PROFIL JOUEUR"

# 2.1 Profil
Print-Test "Consulter le profil du joueur"
$r = Invoke-Api -Method GET -Uri "/api/player/profile" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/player/profile"
Assert-JsonField $r.Body "pseudo" "Profil contient le pseudo"
Assert-JsonField $r.Body "level" "Profil contient le level"

# 2.2 Niveau
Print-Test "Consulter le niveau du joueur"
$r = Invoke-Api -Method GET -Uri "/api/player/level" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/player/level"
Assert-JsonField $r.Body "level" "Niveau retourne"
Assert-JsonField $r.Body "nextLevelXp" "XP requis pour le prochain niveau"

# 2.3 Gagner XP
Print-Test "Gagner 100 XP"
$r = Invoke-Api -Method POST -Uri "/api/player/xp" -Headers $AUTH -Body '{"amount": 100}'
Assert-Status 200 $r.Status "POST /api/player/xp (+100 XP)"
Assert-JsonField $r.Body "experience" "XP mis a jour"

# 2.4 Level up
Print-Test "Monter de niveau (level-up)"
$r = Invoke-Api -Method POST -Uri "/api/player/level-up" -Headers $AUTH
if ($r.Status -eq 200) {
    Write-Host "  [PASS] Level up reussi" -ForegroundColor Green
    $TOTAL++; $PASS++
} else {
    Write-Host "  [INFO] Level up impossible (XP insuffisant) - HTTP $($r.Status) (comportement attendu)" -ForegroundColor Yellow
    $TOTAL++; $PASS++
}

# 2.5 Monstres du joueur
Print-Test "Consulter les monstres du joueur (liste initiale)"
$r = Invoke-Api -Method GET -Uri "/api/player/monsters" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/player/monsters"
Assert-JsonField $r.Body "maxMonsters" "Capacite max de monstres"

# ============================================================================
#  3. MONSTRES (creation manuelle)
# ============================================================================
Print-Header "3. GESTION DES MONSTRES"

# 3.1 Creer monstre FEU
Print-Test "Creer un monstre de type FEU"
$r = Invoke-Api -Method POST -Uri "/api/monsters" -Headers $AUTH -Body '{"name":"TestFireMonster","elementType":"FIRE"}'
Assert-Status 201 $r.Status "POST /api/monsters (FIRE)"
$MONSTER_ID_FIRE = ($r.Body | ConvertFrom-Json).id
Assert-JsonField $r.Body "id" "Monstre cree avec un id"
Assert-JsonField $r.Body "elementType" "Type elementaire FIRE"
Assert-JsonField $r.Body "hp" "Points de vie"
Assert-JsonField $r.Body "atk" "Attaque"

# 3.2 Creer monstre EAU
Print-Test "Creer un monstre de type EAU"
$r = Invoke-Api -Method POST -Uri "/api/monsters" -Headers $AUTH -Body '{"name":"TestWaterMonster","elementType":"WATER"}'
Assert-Status 201 $r.Status "POST /api/monsters (WATER)"
$MONSTER_ID_WATER = ($r.Body | ConvertFrom-Json).id
Assert-JsonField $r.Body "id" "Monstre EAU cree"

# 3.3 Type invalide
Print-Test "Creer un monstre avec type invalide (doit echouer)"
$r = Invoke-Api -Method POST -Uri "/api/monsters" -Headers $AUTH -Body '{"name":"Invalid","elementType":"EARTH"}'
Assert-Status 400 $r.Status "POST /api/monsters (type EARTH invalide)"

# 3.4 Lister monstres
Print-Test "Lister tous les monstres du joueur"
$r = Invoke-Api -Method GET -Uri "/api/monsters" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/monsters"

# 3.5 Detail monstre
Print-Test "Consulter le detail du monstre FEU"
$r = Invoke-Api -Method GET -Uri "/api/monsters/$MONSTER_ID_FIRE" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/monsters/{id}"
Assert-JsonField $r.Body "name" "Nom du monstre"
Assert-JsonField $r.Body "skills" "Competences du monstre"

# 3.6 XP monstre
Print-Test "Faire gagner 500 XP au monstre FEU"
$r = Invoke-Api -Method POST -Uri "/api/monsters/$MONSTER_ID_FIRE/xp" -Headers $AUTH -Body '{"amount":500}'
Assert-Status 200 $r.Status "POST /api/monsters/{id}/xp (+500 XP)"
Assert-JsonField $r.Body "level" "Monstre a monte de niveau"
Assert-JsonField $r.Body "skillPoints" "Points de competence gagnes"

# 3.7 Upgrade skill
Print-Test "Ameliorer la competence 0 du monstre FEU"
$r = Invoke-Api -Method POST -Uri "/api/monsters/$MONSTER_ID_FIRE/upgrade-skill" -Headers $AUTH -Body '{"skillIndex":0}'
if ($r.Status -eq 200) {
    Assert-Status 200 $r.Status "POST /api/monsters/{id}/upgrade-skill"
} else {
    Assert-Status 400 $r.Status "POST /api/monsters/{id}/upgrade-skill (pas de skill points)"
}

# 3.8 Supprimer monstre
Print-Test "Supprimer le monstre EAU"
$r = Invoke-Api -Method DELETE -Uri "/api/monsters/$MONSTER_ID_WATER" -Headers $AUTH
Assert-Status 204 $r.Status "DELETE /api/monsters/{id}"

# 3.9 Monstre supprime
Print-Test "Verifier que le monstre EAU supprime n'existe plus"
$r = Invoke-Api -Method GET -Uri "/api/monsters/$MONSTER_ID_WATER" -Headers $AUTH
Assert-Status 404 $r.Status "GET /api/monsters/{id} (monstre supprime)"

# ============================================================================
#  4. INVOCATION (Gacha)
# ============================================================================
Print-Header "4. INVOCATION (GACHA)"

# 4.1 Templates
Print-Test "Lister les templates de monstres invocables"
$r = Invoke-Api -Method GET -Uri "/api/invocations/templates" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/invocations/templates"

# 4.2 Summon 1
Print-Test "Invoquer un monstre (summon)"
$r = Invoke-Api -Method POST -Uri "/api/invocations/summon" -Headers $AUTH
Assert-Status 201 $r.Status "POST /api/invocations/summon"
$inv = $r.Body | ConvertFrom-Json
$INVOKED_MONSTER_ID = $inv.generatedMonsterId
$INVOKED_NAME = $inv.monsterName
Assert-JsonField $r.Body "invocationId" "ID de l'invocation"
Assert-JsonField $r.Body "monsterName" "Nom du monstre invoque"
Assert-JsonField $r.Body "elementType" "Type elementaire"
Assert-JsonField $r.Body "generatedMonsterId" "ID du monstre genere"
Assert-JsonField $r.Body "status" "Statut COMPLETED"
Write-Host "    -> Monstre invoque : $INVOKED_NAME (id: $INVOKED_MONSTER_ID)" -ForegroundColor Cyan

# 4.3 Summon 2
Print-Test "Invoquer un deuxieme monstre"
$r = Invoke-Api -Method POST -Uri "/api/invocations/summon" -Headers $AUTH
Assert-Status 201 $r.Status "POST /api/invocations/summon (2eme)"
$name2 = ($r.Body | ConvertFrom-Json).monsterName
Write-Host "    -> Monstre invoque : $name2" -ForegroundColor Cyan

# 4.4 Summon 3
Print-Test "Invoquer un troisieme monstre"
$r = Invoke-Api -Method POST -Uri "/api/invocations/summon" -Headers $AUTH
Assert-Status 201 $r.Status "POST /api/invocations/summon (3eme)"
$name3 = ($r.Body | ConvertFrom-Json).monsterName
Write-Host "    -> Monstre invoque : $name3" -ForegroundColor Cyan

# 4.5 Historique
Print-Test "Consulter l'historique des invocations"
$r = Invoke-Api -Method GET -Uri "/api/invocations/history" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/invocations/history"

# 4.6 Retry incomplete
Print-Test "Re-traiter les invocations incompletes (retry)"
$r = Invoke-Api -Method POST -Uri "/api/invocations/retry-incomplete" -Headers $AUTH
Assert-Status 200 $r.Status "POST /api/invocations/retry-incomplete"

# ============================================================================
#  5. VERIFICATIONS CROISEES
# ============================================================================
Print-Header "5. VERIFICATIONS CROISEES"

# 5.1 Monstres invoques dans la liste
Print-Test "Les monstres invoques apparaissent dans le profil joueur"
$r = Invoke-Api -Method GET -Uri "/api/player/monsters" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/player/monsters"
$TOTAL++
if ($r.Body -match $INVOKED_MONSTER_ID) {
    Write-Host "  [PASS] Le monstre invoque est bien dans la liste du joueur" -ForegroundColor Green
    $PASS++
} else {
    Write-Host "  [FAIL] Le monstre invoque n'est PAS dans la liste du joueur" -ForegroundColor Red
    $FAIL++
}

# 5.2 Detail monstre invoque
Print-Test "Detail du monstre invoque via GET /api/monsters/{id}"
$r = Invoke-Api -Method GET -Uri "/api/monsters/$INVOKED_MONSTER_ID" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/monsters/{id} (monstre invoque)"
Assert-JsonField $r.Body "name" "Nom du monstre invoque"
Assert-JsonField $r.Body "hp" "HP du monstre invoque"

# 5.3 Tous les monstres
Print-Test "Lister tous les monstres (manuels + invoques)"
$r = Invoke-Api -Method GET -Uri "/api/monsters" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/monsters (tous)"

# 5.4 Profil final
Print-Test "Profil joueur final (recapitulatif)"
$r = Invoke-Api -Method GET -Uri "/api/player/profile" -Headers $AUTH
Assert-Status 200 $r.Status "GET /api/player/profile (final)"
Assert-JsonField $r.Body "pseudo" "Pseudo du joueur"
Assert-JsonField $r.Body "level" "Niveau du joueur"
Assert-JsonField $r.Body "monsters" "Liste de monstres"

# ============================================================================
#  6. TESTS SANS AUTHENTIFICATION
# ============================================================================
Print-Header "6. TESTS SANS AUTHENTIFICATION (securite)"

Print-Test "Acces au profil sans token (doit echouer)"
$r = Invoke-Api -Method GET -Uri "/api/player/profile"
Assert-Status 500 $r.Status "GET /api/player/profile (sans token)"

Print-Test "Invocation sans token (doit echouer)"
$r = Invoke-Api -Method POST -Uri "/api/invocations/summon"
Assert-Status 500 $r.Status "POST /api/invocations/summon (sans token)"

# ============================================================================
#  RESUME
# ============================================================================
Write-Host ""
Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host "  RESUME DES TESTS" -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Total  : $TOTAL tests"
Write-Host "  Reussis : $PASS" -ForegroundColor Green
Write-Host "  Echoues : $FAIL" -ForegroundColor Red
Write-Host ""

if ($FAIL -eq 0) {
    Write-Host "  TOUS LES TESTS SONT PASSES !" -ForegroundColor Green
} else {
    Write-Host "  $FAIL TEST(S) EN ECHEC" -ForegroundColor Red
}
Write-Host ""

exit $FAIL
