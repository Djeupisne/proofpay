# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copier le pom.xml
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copier TOUS les fichiers sources
COPY src ./src

# Vérifier que les fichiers storage sont présents
RUN ls -la src/main/java/com/proofpay/attachment/storage/ || echo "Storage directory not found"

# Compiler
RUN mvn -B clean package -DskipTests

# --- Run stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Installation de wget pour healthcheck
RUN apk add --no-cache wget

# Utilisateur non-root
RUN addgroup -S proofpay && adduser -S proofpay -G proofpay \
    && mkdir -p /app/storage/attachments \
    && chown -R proofpay:proofpay /app

# Copier le JAR depuis le build
COPY --from=build --chown=proofpay:proofpay /app/target/*.jar app.jar

USER proofpay
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=render -jar app.jar"]