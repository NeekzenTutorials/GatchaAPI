# Gacha Frontend (Vue 3)

Frontend immersif pour votre backend microservices (`CentralAPI` sur `http://localhost:8080`).

## Stack
- Vue 3 (Composition API)
- Vue Router
- Pinia
- Axios (avec intercepteur JWT)
- SCSS moderne + animations

## Lancer le projet
```bash
npm install
npm run dev
```

Application disponible sur `http://localhost:5173`.

## Fonctionnalités
- Authentification complète (`/api/auth/login`, `/api/auth/register`, `/api/auth/validate`)
- Stockage JWT en `localStorage`
- Routes protégées avec redirection automatique
- Profil joueur (niveau, XP animée, limite de monstres)
- Collection de monstres (cartes stylées + modal détail)
- Invocation gacha immersive (reveal animé + glow selon rareté)
- Historique des invocations
- Loader global et notifications toast
- Responsive mobile

## Structure
- `src/components`
- `src/views`
- `src/services`
- `src/stores`
- `src/router`
- `src/assets`

## Notes API
Le proxy Vite redirige automatiquement les appels `/api/*` vers `http://localhost:8080` (voir `vite.config.js`).
