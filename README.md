# TaskFlow

Gestionnaire de tâches sécurisé. Chaque utilisateur ne voit que ses propres tâches.

**Technologies :** Vue 3 · Spring Boot 3  · Keycloak · PostgreSQL · Docker · GitHub Actions · Render
**Prérequis : Version java 17+, docker desktop, node installer(pour executer les commandes: npm test, npm run build et npm run dev ) 
## En ligne

| | Adresse |
|---|---|
| Application | https://taskflow-front-lina.onrender.com |
| API | https://taskflow-api-lina.onrender.com |
| Health check | https://taskflow-api-lina.onrender.com/actuator/health |
| Swagger | https://taskflow-api-lina.onrender.com/swagger-ui/index.html |
| Keycloak | https://taskflow-keycloak-lina.onrender.com |

**Comptes de test**

| Utilisateur | Mot de passe | Rôles |
|---|---|---|
| `alice` | `alicealice` | user |
| `bob` | `bobbob` | user, admin |

> L'API est protégée : un `401` sans jeton est normal. Tester via l'application.

## Lancer en local

Prérequis : Docker Desktop.

```bash
cp .env.example .env     # PowerShell : Copy-Item .env.example .env
docker compose up --build
```

- Application : http://localhost:5173
- API : http://localhost:8081 (health : `/actuator/health`)
- Keycloak : http://localhost:8180

Le realm Keycloak est importé automatiquement au démarrage. Arrêt : `docker compose down`.

## Configuration

Aucune adresse ni secret n'est écrit dans le code : tout vient de variables d'environnement.
Copiez `.env.example` en `.env` (jamais versionné) et changez les mots de passe.

| Variable | Rôle |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | base PostgreSQL |
| `KEYCLOAK_ADMIN`, `KEYCLOAK_ADMIN_PASSWORD` | administrateur Keycloak |
| `KEYCLOAK_PUBLIC_URL`, `API_PUBLIC_URL`, `FRONTEND_URL` | adresses publiques |

Pour lancer l'API depuis IntelliJ, définissez aussi `DB_URL`, `DB_USER`, `DB_PASSWORD`, `KEYCLOAK_ISSUER_URI` et `CORS_ALLOWED_ORIGINS`.

## Tests et intégration continue

```bash
cd taskflowBack && mvn verify                                  # API
cd taskflow-front && npm ci && npm run build && npm test       # frontend
```

GitHub Actions (`.github/workflows/ci.yml`) compile les deux projets et lance tous les tests à chaque push, puis démarre la pile Docker complète et vérifie le health check.

## Déploiement (Render)

Décrit dans `render.yaml` : base PostgreSQL, Keycloak, API et frontend, en HTTPS.
Les secrets sont définis dans Render, pas dans le dépôt.
Le déploiement est automatique sur la branche principale, seulement si les tests passent.

## Limites de l'hébergement gratuit

- Les services s'endorment après environ 15 minutes d'inactivité : la première requête peut prendre une minute.
- Keycloak est lent à démarrer.
- Lancement de l'application est lent.
