package com.proofpay.common.util;

import java.util.regex.Pattern;

/**
 * Normalise les numéros de téléphone avant toute utilisation comme clé
 * (recherche utilisateur, stockage OTP). Sans cette normalisation, un espace
 * ou tiret en plus entre la demande d'OTP et sa vérification (fréquent avec
 * la saisie mobile ou l'autocomplétion) fait échouer silencieusement la
 * recherche et renvoie à tort "code invalide".
 */
public final class PhoneNormalizer {

    // 8 à 15 chiffres (norme E.164), + optionnel, jamais vide/blanc.
    // Sans ce contrôle de FORMAT (en plus de la normalisation), une chaîne vide
    // ou composée uniquement d'espaces passait à travers et créait un compte
    // avec un numéro de téléphone vide — faille corrigée ici.
    private static final Pattern VALID_PHONE = Pattern.compile("^\\+?[0-9]{8,15}$");

    private PhoneNormalizer() {
    }

    public static String normalize(String rawPhone) {
        if (rawPhone == null) {
            return null;
        }
        return rawPhone.trim().replaceAll("[\\s\\-().]", "");
    }

    /** À appeler systématiquement après normalize() avant tout usage métier. */
    public static boolean isValid(String normalizedPhone) {
        return normalizedPhone != null && VALID_PHONE.matcher(normalizedPhone).matches();
    }
}
