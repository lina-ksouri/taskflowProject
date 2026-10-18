# ---- Étape 1 : compilation avec Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
# Les tests sont exécutés par la CI (GitHub Actions) ; l'image ne fait que compiler.
RUN mvn -B -q -DskipTests package

# ---- Étape 2 : image d'exécution légère, sans root ----
FROM eclipse-temurin:17-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --no-create-home appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
# Retire d'éventuelles fins de ligne Windows (CRLF) puis rend le script exécutable
RUN sed -i 's/\r$//' /app/docker-entrypoint.sh && chmod 755 /app/docker-entrypoint.sh

# Mémoire limitée (hébergement gratuit : 512 Mo) : tas à 70 % de la RAM, ramasse-miettes léger
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"

USER appuser
EXPOSE 8081

# Health check de l'API (inclut la connexion à la base de données)
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS "http://localhost:${PORT:-8081}/actuator/health" || exit 1

ENTRYPOINT ["/app/docker-entrypoint.sh"]
