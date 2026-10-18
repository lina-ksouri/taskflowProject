# Image Keycloak de TaskFlow : le realm "taskflow" est embarqué et importé au démarrage.
ARG KEYCLOAK_VERSION=26.8.1

# --- Étape 0 : prépare les scripts (retire les fins de ligne Windows qui casseraient le démarrage) ---
FROM alpine:3.20 AS scripts
COPY entrypoint.sh healthcheck.sh /scripts/
RUN sed -i 's/\r$//' /scripts/*.sh && chmod 755 /scripts/*.sh

# --- Étape 1 : "build" de Keycloak avec les options figées (PostgreSQL, health check) ---
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} AS builder
ENV KC_DB=postgres \
    KC_HEALTH_ENABLED=true
RUN /opt/keycloak/bin/kc.sh build

# --- Étape 2 : image finale ---
FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION}
COPY --from=builder /opt/keycloak/ /opt/keycloak/
# Le realm est lu au démarrage grâce à l'option --import-realm (dossier data/import).
# Les ${FRONTEND_URL} qu'il contient sont remplacés par la variable d'environnement du même nom.
COPY --chown=1000:0 realm-taskflow.json /opt/keycloak/data/import/taskflow-realm.json
COPY --from=scripts /scripts/ /opt/keycloak/
ENV KC_DB=postgres \
    KC_HEALTH_ENABLED=true
ENTRYPOINT ["/opt/keycloak/entrypoint.sh"]
CMD ["start", "--optimized", "--import-realm"]
