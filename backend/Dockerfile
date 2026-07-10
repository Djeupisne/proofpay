# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copier le pom.xml
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copier TOUS les fichiers sources (forcé)
COPY src/ ./src/

# Vérification explicite des fichiers
RUN echo "=== Vérification des fichiers sources ===" && \
    ls -la src/main/java/com/proofpay/attachment/ && \
    echo "=== Vérification du dossier storage ===" && \
    ls -la src/main/java/com/proofpay/attachment/storage/ || echo "storage not found"

# Nettoyer le cache Maven
RUN rm -rf ~/.m2/repository/

# Compiler
RUN mvn -B clean compile package -DskipTests

# --- Run stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache wget
RUN addgroup -S proofpay && adduser -S proofpay -G proofpay \
    && mkdir -p /app/storage/attachments \
    && chown -R proofpay:proofpay /app

COPY --from=build --chown=proofpay:proofpay /app/target/*.jar app.jar

USER proofpay
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=render -jar app.jar"]