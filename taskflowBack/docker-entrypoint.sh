#!/bin/sh
# Script de démarrage de l'API : adapte la configuration à l'environnement, puis lance Java.
set -eu

# Render fournit DATABASE_URL = postgres://utilisateur:motdepasse@hôte[:port]/base
# alors que Spring attend DB_URL (URL JDBC), DB_USER et DB_PASSWORD.
if [ -z "${DB_URL:-}" ] && [ -n "${DATABASE_URL:-}" ]; then
  rest="${DATABASE_URL#*://}"      # utilisateur:motdepasse@hôte[:port]/base
  creds="${rest%%@*}"              # utilisateur:motdepasse
  DB_USER="${creds%%:*}"
  DB_PASSWORD="${creds#*:}"
  DB_URL="jdbc:postgresql://${rest#*@}"
  export DB_URL DB_USER DB_PASSWORD
fi

# shellcheck disable=SC2086
exec java $JAVA_OPTS -jar /app/app.jar
