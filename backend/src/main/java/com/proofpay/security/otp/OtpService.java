package com.proofpay.security.otp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Génération et vérification d'OTP pour l'inscription/connexion et les
 * actions sensibles (règle métier #24 : les actions sensibles doivent être
 * sécurisées par OTP ou authentification).
 * Stockage MVP en mémoire ; à migrer vers Redis pour la production.
 */
@Service
public class OtpService {

    private final int length;
    private final long ttlMinutes;
    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();

    public OtpService(@Value("${proofpay.otp.length}") int length,
                       @Value("${proofpay.otp.ttl-minutes}") long ttlMinutes) {
        this.length = length;
        this.ttlMinutes = ttlMinutes;
    }

    public String generate(String phone) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        store.put(phone, new OtpEntry(code.toString(), Instant.now().plusSeconds(ttlMinutes * 60)));
        return code.toString();
    }

    public boolean verify(String phone, String code) {
        OtpEntry entry = store.get(phone);
        if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
            return false;
        }
        boolean matches = entry.code().equals(code);
        if (matches) {
            store.remove(phone);
        }
        return matches;
    }

    private record OtpEntry(String code, Instant expiresAt) {
    }
}
