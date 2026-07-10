package com.proofpay.common.util;

import java.security.SecureRandom;

/**
 * Génère les références publiques uniques (transactions.public_ref) —
 * règle métier #17 : chaque transaction doit avoir une référence unique.
 */
public final class ReferenceGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private ReferenceGenerator() {
    }

    public static String generateTransactionRef() {
        StringBuilder sb = new StringBuilder("PP-");
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
