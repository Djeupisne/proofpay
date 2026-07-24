package com.proofpay.attachment.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class LocalStorageProvider implements StorageProvider {

    private final Path basePath;

    public LocalStorageProvider(@Value("${proofpay.storage.local-base-path:./storage/attachments}") String basePath) {
        this.basePath = Paths.get(basePath);
        try {
            Files.createDirectories(this.basePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create storage directory: " + basePath, e);
        }
    }

    @Override
    public String store(String fileName, String mimeType, InputStream content) throws Exception {
        Files.createDirectories(basePath);
        String storedName = UUID.randomUUID() + "-" + fileName;
        Path target = basePath.resolve(storedName);
        try (FileOutputStream out = new FileOutputStream(target.toFile())) {
            content.transferTo(out);
        }
        return target.toString();
    }

    @Override
    public InputStream retrieve(String storagePath) throws Exception {
        return new FileInputStream(storagePath);
    }

    @Override
    public void delete(String storagePath) throws Exception {
        Files.deleteIfExists(Paths.get(storagePath));
    }
}
