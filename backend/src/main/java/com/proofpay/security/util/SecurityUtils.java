package com.proofpay.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Extrait l'identité de l'utilisateur authentifié depuis le SecurityContext
 * (peuplé par JwtAuthenticationFilter), plutôt que de faire confiance à un
 * identifiant fourni par le client dans la requête — un uploadedBy/userId
 * passé en paramètre de requête est trivialement falsifiable.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString((String) auth.getPrincipal());
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
