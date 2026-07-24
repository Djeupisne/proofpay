package com.proofpay.attachment.storage;

import java.io.InputStream;

public interface StorageProvider {
    String store(String fileName, String mimeType, InputStream content) throws Exception;
    InputStream retrieve(String storagePath) throws Exception;
    void delete(String storagePath) throws Exception;
}
