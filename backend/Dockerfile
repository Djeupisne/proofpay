# --- Build stage ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copier le pom.xml
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copier le code source
COPY src ./src

# CRÉER LES FICHIERS STORAGE MANQUANTS DIRECTEMENT
RUN mkdir -p src/main/java/com/proofpay/attachment/storage && \
    echo 'package com.proofpay.attachment.storage;' > src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo '' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo 'import java.io.InputStream;' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo '' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo 'public interface StorageProvider {' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo '    String store(String fileName, String mimeType, InputStream content) throws Exception;' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo '    InputStream retrieve(String storagePath) throws Exception;' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo '    void delete(String storagePath) throws Exception;' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java && \
    echo '}' >> src/main/java/com/proofpay/attachment/storage/StorageProvider.java

RUN mkdir -p src/main/java/com/proofpay/attachment/storage && \
    echo 'package com.proofpay.attachment.storage;' > src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo 'import org.springframework.beans.factory.annotation.Value;' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo 'import org.springframework.stereotype.Component;' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo 'import java.io.FileInputStream;' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo 'import java.io.FileOutputStream;' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo 'import java.io.InputStream;' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo 'import java.nio.file.Files;' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo 'import java.nio.file.Path;' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo 'import java.util.UUID;' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '@Component' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo 'public class LocalStorageProvider implements StorageProvider {' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    private final Path basePath;' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    public LocalStorageProvider(@Value("${proofpay.storage.local-base-path}") String basePath) {' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '        this.basePath = Path.of(basePath);' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    }' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    @Override' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    public String store(String fileName, String mimeType, InputStream content) throws Exception {' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '        Files.createDirectories(basePath);' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '        String storedName = UUID.randomUUID() + "-" + fileName;' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '        Path target = basePath.resolve(storedName);' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '        try (FileOutputStream out = new FileOutputStream(target.toFile())) {' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '            content.transferTo(out);' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '        }' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '        return target.toString();' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    }' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    @Override' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    public InputStream retrieve(String storagePath) throws Exception {' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '        return new FileInputStream(storagePath);' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    }' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    @Override' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    public void delete(String storagePath) throws Exception {' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '        Files.deleteIfExists(Path.of(storagePath));' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '    }' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java && \
    echo '}' >> src/main/java/com/proofpay/attachment/storage/LocalStorageProvider.java

# 🔥 VÉRIFICATION : Afficher la version de Lombok
RUN echo "=== Vérification de Lombok ===" && \
    mvn dependency:tree | grep lombok || echo "Lombok non trouvé dans les dépendances"

# 🔥 Compilation avec Lombok activé
RUN echo "=== Compilation avec Lombok ===" && \
    mvn -B clean compile -DskipTests

# Vérifier que les classes générées existent
RUN echo "=== Vérification des classes générées ===" && \
    ls -la target/classes/com/proofpay/user/domain/ || echo "Dossier non trouvé" && \
    ls -la target/classes/com/proofpay/subscription/domain/ || echo "Dossier non trouvé"

# Package
RUN mvn -B package -DskipTests

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
