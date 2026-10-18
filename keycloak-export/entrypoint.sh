#!/bin/bash
# Script de démarrage de Keycloak : adapte la configuration à l'environnement, puis lance Keycloak.
set -euo pipefail

# 1) Port HTTP : Render impose la variable PORT ; en local on garde 8080.
export KC_HTTP_PORT="${PORT:-${KC_HTTP_PORT:-8080}}"

# 2) Base de données : deux façons de la renseigner.
#    - KC_DB_URL + KC_DB_USERNAME + KC_DB_PASSWORD (Docker Compose)
#    - DATABASE_URL au format postgres://utilisateur:motdepasse@hôte[:port]/base (Render)
if [[ -z "${KC_DB_URL:-}" && -n "${DATABASE_URL:-}" ]]; then
  rest="${DATABASE_URL#*://}"      # utilisateur:motdepasse@hôte[:port]/base
  creds="${rest%%@*}"              # utilisateur:motdepasse
  export KC_DB_USERNAME="${creds%%:*}"
  export KC_DB_PASSWORD="${creds#*:}"
  export KC_DB_URL="jdbc:postgresql://${rest#*@}"
fi

# 3) Démarrage de Keycloak (les arguments viennent du CMD du Dockerfile).
exec /opt/keycloak/bin/kc.sh "$@"
