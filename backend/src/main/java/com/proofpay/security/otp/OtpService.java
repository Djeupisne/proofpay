package com.proofpay.security.otp;

import com.proofpay.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final int length;
    private final long ttlMinutes;
    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();

    // ⬇️ NOUVEAU : Limite de tentatives par numéro
    private final ConcurrentHashMap<String, AttemptInfo> attemptStore = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MINUTES = 10;

    public OtpService(@Value("${proofpay.otp.length}") int length,
                      @Value("${proofpay.otp.ttl-minutes}") long ttlMinutes) {
        this.length = length;
        this.ttlMinutes = ttlMinutes;
    }

    public String generate(String phone) {
        // Vérifier la limite de tentatives
        checkRateLimit(phone);

        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        store.put(phone, new OtpEntry(code.toString(), Instant.now().plusSeconds(ttlMinutes * 60)));
        return code.toString();
    }

    public boolean verify(String phone, String code) {
        // Vérifier si le numéro est bloqué
        if (isBlocked(phone)) {
            throw new BusinessException("OTP_BLOCKED",
                    "Trop de tentatives. Veuillez réessayer dans " + BLOCK_DURATION_MINUTES + " minutes.");
        }

        OtpEntry entry = store.get(phone);
        if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
            // Incrémenter le compteur d'échecs
            incrementAttempts(phone);
            return false;
        }

        boolean matches = entry.code().equals(code);
        if (matches) {
            store.remove(phone);
            // Réinitialiser les tentatives en cas de succès
            attemptStore.remove(phone);
        } else {
            // Incrémenter le compteur d'échecs
            incrementAttempts(phone);
        }
        return matches;
    }

    // ⬇️ NOUVELLES MÉTHODES POUR LA LIMITE DE TENTATIVES

    private void checkRateLimit(String phone) {
        AttemptInfo info = attemptStore.get(phone);
        if (info != null && info.attempts >= MAX_ATTEMPTS) {
            if (Instant.now().isBefore(info.blockedUntil)) {
                throw new BusinessException("OTP_BLOCKED",
                        "Trop de tentatives. Veuillez réessayer dans " +
                                (BLOCK_DURATION_MINUTES - info.getElapsedMinutes()) + " minutes.");
            } else {
                // Le blocage est expiré, réinitialiser
                attemptStore.remove(phone);
            }
        }
    }

    private boolean isBlocked(String phone) {
        AttemptInfo info = attemptStore.get(phone);
        return info != null && info.attempts >= MAX_ATTEMPTS &&
                Instant.now().isBefore(info.blockedUntil);
    }

    private void incrementAttempts(String phone) {
        AttemptInfo info = attemptStore.computeIfAbsent(phone, k ->
                new AttemptInfo(0, Instant.now().plusMinutes(BLOCK_DURATION_MINUTES)));
        info.attempts++;
        if (info.attempts >= MAX_ATTEMPTS) {
            info.blockedUntil = Instant.now().plusMinutes(BLOCK_DURATION_MINUTES);
        }
        attemptStore.put(phone, info);
    }

    // ⬇️ NOUVELLE CLASSE INTERNE
    private static class AttemptInfo {
        int attempts;
        Instant blockedUntil;

        AttemptInfo(int attempts, Instant blockedUntil) {
            this.attempts = attempts;
            this.blockedUntil = blockedUntil;
        }

        long getElapsedMinutes() {
            return java.time.Duration.between(Instant.now(), blockedUntil).toMinutes();
        }
    }

    private record OtpEntry(String code, Instant expiresAt) {
    }
}