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

# Utilisateur non-root
RUN addgroup -S proofpay && adduser -S proofpay -G proofpay \
    && mkdir -p /app/storage/attachments \
    && chown -R proofpay:proofpay /app

COPY --from=build --chown=proofpay:proofpay /app/target/*.jar app.jar

USER proofpay
EXPOSE 8080

# Correction : Utiliser wget au lieu de curl (curl n'est pas installé par défaut)
# ou installer curl dans l'image
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

# Correction : Passer le profil "prod" explicitement
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar app.jar"]