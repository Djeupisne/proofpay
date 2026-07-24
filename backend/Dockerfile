# --- Build stage ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

# Créer les fichiers storage manquants
RUN mkdir -p src/main/java/com/proofpay/attachment/storage && \
    echo 'package com.proofpay.attachment.storage;' > src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo 'public interface StorageProvider {' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo '    String store(String fileName, String mimeType, java.io.InputStream content) throws Exception;' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo '    java.io.InputStream retrieve(String storagePath) throws Exception;' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo '    void delete(String storagePath) throws Exception;' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo '}' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java

# Compilation simple (plus de Lombok)
RUN mvn -B clean package -DskipTests

# --- Run stage ---
FROM eclipse-temurin:17-jre-alpine
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
