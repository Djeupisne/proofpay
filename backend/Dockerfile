# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Run stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Utilisateur non-root : ne jamais faire tourner l'app en root en production.
RUN addgroup -S proofpay && adduser -S proofpay -G proofpay \
    && mkdir -p /app/storage/attachments \
    && chown -R proofpay:proofpay /app

COPY --from=build --chown=proofpay:proofpay /app/target/*.jar app.jar

USER proofpay
EXPOSE 8080

# Nécessite curl pour le healthcheck (déjà présent sur eclipse-temurin-alpine récents;
# sinon: RUN apk add --no-cache curl avant USER proofpay)
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

# JAVA_OPTS permet d'ajuster la mémoire au conteneur (ex. Render: -Xmx512m)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
